package ru.neoflex.scammertracking.analyzer.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.AnalyzeModel;
import ru.neoflex.scammertracking.analyzer.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.util.Map;

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
                .bodyToFlux(Object.class)
                .flatMap(value -> {
                    AggregateLastPaymentDto aggregateModel = new AggregateLastPaymentDto();
                    ((Map) value).forEach((k, v) -> {
                        if ("paymentRequest".equals(k)) {
                            PaymentRequestDto paymentRequest = new ObjectMapper().convertValue(v, PaymentRequestDto.class);
                            aggregateModel.setPaymentRequest(paymentRequest);
                        } else if ("paymentResponse".equals(k)) {
                            LastPaymentResponseDto paymentResponseDto = new ObjectMapper().convertValue(v, LastPaymentResponseDto.class);
                            aggregateModel.setPaymentResponse(paymentResponseDto);
                        }
                    });
                    return Mono.just(aggregateModel);
                });
    }

    @Override
    public Flux<Object> savePayment(Flux<SavePaymentRequestDto> savePaymentRequest) {
        return WebClient
                .create("http://localhost:8082/payment/save")
                .post()
                .body(savePaymentRequest, Flux.class)
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(x ->
                        Mono.just(x));
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
