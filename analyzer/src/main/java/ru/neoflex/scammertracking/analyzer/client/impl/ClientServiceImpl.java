package ru.neoflex.scammertracking.analyzer.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

@Service
@Slf4j
public class ClientServiceImpl implements ClientService {

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;

    @Override
    public Flux<AggregateLastPaymentDto> getLastPayment(Flux<AggregateLastPaymentDto> paymentRequests) {
        log.info("Input getLastPayment. received list of payments");

        return WebClient
                .create(paymentServiceHostPort)
                .post()
                .uri(ConfigUtil.getLastPaymentEndpoint())
                .body(paymentRequests, Flux.class)
                .retrieve()
                .bodyToFlux(AggregateLastPaymentDto.class)
                .flatMap(value -> {
                    AggregateLastPaymentDto aggregateModel = new AggregateLastPaymentDto(value.getPaymentRequest(), value.getPaymentResponse());
                    return Mono.just(aggregateModel);
                });
    }

    @Override
    public Flux<SavePaymentResponseDto> savePayment(Flux<SavePaymentRequestDto> savePaymentRequest) {
        return WebClient
                .create("http://localhost:8082/payment/save")
                .post()
                .body(savePaymentRequest, Flux.class)
                .retrieve()
                .bodyToFlux(SavePaymentResponseDto.class);
    }

//        lastPaymentResponse.subscribe(new BaseSubscriber<AggregateLastPaymentDto>() {
//            @Override
//            protected void hookOnSubscribe(Subscription subscription) {
//                super.hookOnSubscribe(subscription);
//            }
//
//            @Override
//            protected void hookOnNext(AggregateLastPaymentDto value) {
//                super.hookOnNext(value);
//            }
//
//            @Override
//            protected void hookOnComplete() {
//                super.hookOnComplete();
//            }
//
//            @Override
//            protected void hookOnError(Throwable throwable) {
//                super.hookOnError(throwable);
//            }
//        });
}
