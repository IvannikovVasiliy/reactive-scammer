package ru.neoflex.scammertracking.analyzer.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;
    @Value("${app.lastPayment}")
    private String lastPaymentEndpoint;
    @Value("${app.savePayment}")
    private String savePaymentEndpoint;

    @Override
    public Flux<AggregateGetLastPaymentDto> getLastPayment(Flux<AggregateGetLastPaymentDto> paymentRequests) {
        log.info("Input getLastPayment. received list of payments");

        return WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(lastPaymentEndpoint)
                .body(paymentRequests, Flux.class)
                .retrieve()
                .bodyToFlux(AggregateGetLastPaymentDto.class)
                .flatMap(value -> {
                    AggregateGetLastPaymentDto aggregateModel = new AggregateGetLastPaymentDto(value.getPaymentRequest(), value.getPaymentResponse());
                    return Mono.just(aggregateModel);
                });
    }

    @Override
    public Flux<SavePaymentResponseDto> savePayment(Flux<SavePaymentRequestDto> savePaymentRequest) {
        return WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(savePaymentEndpoint)
                .body(savePaymentRequest, Flux.class)
                .retrieve()
                .bodyToFlux(SavePaymentResponseDto.class);
    }
}
