package ru.neoflex.scammertracking.analyzer.check.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.PaymentChecker;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.SuspiciousPaymentException;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.router.SavePaymentRouter;
import ru.neoflex.scammertracking.analyzer.util.CustomLogs;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCheckerImpl implements PaymentChecker {

    private final GeoAnalyzer geoAnalyzer;
    private final SavePaymentRouter savePaymentRouter;
    private final PaymentProducer paymentProducer;
    private final SourceMapperImplementation sourceMapper;
    private final ObjectMapper objectMapper;

    public boolean preCheckSuspicious(PaymentRequestDto paymentRequest) {
        CustomLogs.logPreCheckSuspicious(paymentRequest);

        if (paymentRequest.getPayerCardNumber().length() < 6) {
            log.warn("Result validating. The message is suspicious, because the length of payerCardNumber is too short");
            return true;
        }
        if (paymentRequest.getReceiverCardNumber().length() < 6) {
            log.warn("Result validating. The message is suspicious, because the length of receiverCardNumber is too short");
            return true;
        }
        if (LocalDateTime.now().isBefore(paymentRequest.getDate())) {
            log.warn("Result validating. The message is suspicious, because date of paymentRequest more than current datetime");
            return true;
        }

        log.info("Result validating. The message is valid");
        return false;
    }

    public void checkLastPayment(Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux) {
        Flux<SavePaymentRequestDto> savePaymentFlux = aggregatePaymentsFlux
                .flatMap(value -> {
                    boolean isTrusted;
                    if (value.getPaymentResponse() != null) {
                        isTrusted = geoAnalyzer.checkPayment(value.getPaymentResponse(), value.getPaymentRequest());
                        if (!isTrusted) {
                            byte[] suspiciousPaymentBytes;
                            try {
                                suspiciousPaymentBytes = objectMapper.writeValueAsBytes(value.getPaymentRequest());
                            } catch (JsonProcessingException e) {
                                return Flux.error(new RuntimeException(e));
                            }
                            paymentProducer.sendSuspiciousMessage(value.getPaymentRequest().getPayerCardNumber(), suspiciousPaymentBytes);
                            return Flux.error(new SuspiciousPaymentException("Payment with id={} is suspicious, because it is failed in geo-analyzing-stage"));
                        }
                    }

                    SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(value.getPaymentRequest());
                    return Mono.just(savePaymentRequestDto);
                })
                .onErrorResume(err ->
                        Mono.empty());

        savePaymentRouter.savePayment(savePaymentFlux);
    }
}
