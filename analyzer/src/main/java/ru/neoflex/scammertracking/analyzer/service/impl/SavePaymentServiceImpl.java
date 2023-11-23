package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentService;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentServiceImpl implements SavePaymentService {

    private final ClientService clientService;
    private final PaymentProducer paymentProducer;
    private final PaymentCacheService paymentCacheService;
    private final ObjectMapper objectMapper;

    public void savePayment(boolean isTrusted, SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult) {
        log.info("Received. isTrusted={}. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n Payment result={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {}}", isTrusted, savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        paymentResult.setTrusted(isTrusted);
        if (isTrusted) {
            clientService
                    .savePayment(savePaymentRequest)
                    .doOnSuccess(payment -> {
                        log.info("Complete request to save payment to ms-payment");
                        paymentCacheService.saveIfAbsent(savePaymentRequest);
                        paymentProducer.sendCheckedMessage(paymentResult);
                    })
                    .doOnError(throwable -> {
                        log.error("savePayment error. error from ms-payment, because of {}", throwable.getMessage());

                        if (throwable instanceof BadRequestException) {
                            byte[] paymentResultBytes;
                            try {
                                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
                            } catch (JsonProcessingException e) {
                                log.error("Unable to parse paymentResult into bytes");
                                throw new RuntimeException(throwable.getMessage());
                            }
                            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
                            log.info("Response. Sent message in suspicious-topic, BadRequest because of {}", throwable.getMessage());
                        }
                    })
                    .retryWhen(
                            Retry
                                    .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
                                    .filter(throwable -> !(throwable instanceof BadRequestException))
                                    .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
                                        throw new RuntimeException("Error savePayment. External ms-payment failed to process after max retries");
                                    }))
                    )
                    .subscribe();
        } else {
            byte[] paymentResultBytes;
            try {
                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse paymentResult into bytes");
                throw new RuntimeException(e);
            }
            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
            log.info("Response. Sent message in suspicious-topic, because latitude={}, longitude={}",
                    savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude());
        }
    }
}
