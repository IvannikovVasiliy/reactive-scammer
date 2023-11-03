package ru.neoflex.scammertracking.analyzer.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.error.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.error.exception.NotFoundException;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;
import ru.neoflex.scammertracking.analyzer.util.Constants;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;

    @Override
    public Mono<LastPaymentResponseDto> getLastPayment(PaymentRequestDto paymentRequest) {
        log.info("Input getLastPayment. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

        LastPaymentRequestDto lastPaymentRequestDto = new LastPaymentRequestDto(paymentRequest.getPayerCardNumber());

        // try-catch
        Mono<LastPaymentResponseDto> lastPaymentResponse = WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(ConfigUtil.getLastPaymentEndpoint())
                .bodyValue(lastPaymentRequestDto)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.value() == Constants.NOT_FOUND,
                        clientResponse -> {
                            String message = String.format("Payer card number with id=%s not found", paymentRequest.getPayerCardNumber());
                            return Mono.error(new NotFoundException(message));
                        }
                )
                .bodyToMono(LastPaymentResponseDto.class);

        log.info("Output getLastPayment. Success");
        return lastPaymentResponse;
    }

    @Override
    public Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest) {
        log.info("save payment. savePaymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate());

        Mono<Void> savePayment = WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(ConfigUtil.savePaymentEndpoint())
                .bodyValue(savePaymentRequest)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.value() == Constants.BAD_REQUEST,
                        clientResponse -> {
                            String message = String.format("Payer card number with id=%s already exists", savePaymentRequest.getPayerCardNumber());
                            return Mono.error(new BadRequestException(message));
                        }
                )
                .bodyToMono(Void.class);

        log.info("Output savePayment. Success");
        return savePayment;
    }
}
