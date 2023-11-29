package ru.neoflex.scammertracking.analyzer.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.exception.NotFoundException;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;

    @Override
    public Flux<Map.Entry> getLastPayment(List<LastPaymentRequestDto> paymentRequests) {
        log.info("Input getLastPayment. received list of payments");

        Flux<Map.Entry> lastPaymentResponse = WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(ConfigUtil.getLastPaymentEndpoint())
                .bodyValue(paymentRequests)
                .retrieve()
//                .onStatus(
//                        httpStatus -> httpStatus.value() == Constants.NOT_FOUND,
//                        clientResponse -> {
//                            String message = String.format("Payer card number with id=%s not found", 12);
//                            return Mono.error(new NotFoundException(message));
//                        }
//                )
                .bodyToFlux(Map.Entry.class);

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
                .accept(MediaType.ALL)
                .bodyValue(savePaymentRequest)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.value() == Constants.BAD_REQUEST,
                        clientResponse -> {
                            String message = String.format("Payer card number with id=%s already exists", savePaymentRequest.getId());
                            return Mono.error(new BadRequestException(message));
                        }
                )
                .bodyToMono(Void.class);

        log.info("Output savePayment");
        return savePayment;
    }
}
