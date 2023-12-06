package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLastPaymentServiceImpl implements GetLastPaymentService {

    private final SourceMapperImplementation sourceMapper;
    private final ClientService clientService;
    private final GeoAnalyzer geoAnalyzer;
    private final SavePaymentRouter savePaymentRouter;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> process(Flux<AggregateGetLastPaymentDto> paymentRequestsFlux) {

        Flux<AggregateGetLastPaymentDto> fluxNonCache = paymentRequestsFlux.filter(val -> {
            if (val.getPaymentResponse() == null) {
                return true;
            }
            return false;
        });

//        Flux<AggregateGetLastPaymentDto> fluxCache = paymentRequestsFlux.filter(val -> {
//            if (val.getPaymentResponse() != null) {
//                return true;
//            }
//            return false;
//        });

        Flux<AggregateGetLastPaymentDto> f = clientService
                .getLastPayment(fluxNonCache);

//        f.subscribe(new BaseSubscriber<AggregateGetLastPaymentDto>() {
//            @Override
//            protected void hookOnSubscribe(Subscription subscription) {
//                super.hookOnSubscribe(subscription);
//            }
//
//            @Override
//            protected void hookOnNext(AggregateGetLastPaymentDto value) {
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
//                System.out.println();
//            }
//        });

//        checkLastPaymentAsync(fluxCache);
        checkLastPaymentAsync(f);
        return Mono.empty();
    }

    private Mono<Void> checkLastPaymentAsync(Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux) {
        Flux<SavePaymentRequestDto> savePaymentFlux = aggregatePaymentsFlux
                .flatMap(value -> {
                    boolean isTrusted;
                    if (value.getPaymentResponse() == null) {
                        isTrusted = true;
                    } else {
                        isTrusted = geoAnalyzer.checkPayment(value.getPaymentResponse(), value.getPaymentRequest());
                        if (isTrusted == false) {
                            throw new RuntimeException("Payment with id={} is suspicious, because it is failed in geo-analyzing-stage");
                        }
                    }

                    PaymentResponseDto paymentResponseDto = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(value.getPaymentRequest());
                    SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(value.getPaymentRequest());
                    SavePaymentDto savePaymentDto = new SavePaymentDto(isTrusted, savePaymentRequestDto, paymentResponseDto);
                    return Mono.just(savePaymentDto.getSavePaymentRequestDto());
                })
                .onErrorResume(err ->
                        Mono.empty());

        savePaymentRouter.savePayment(savePaymentFlux);

        return Mono.empty();
    }

    private void sendBackoffMessageInKafka(PaymentRequestDto paymentRequest) {
        try {
            byte[] backoffBytesPayment = objectMapper.writeValueAsBytes(paymentRequest);
            paymentProducer.sendBackoffMessage(String.valueOf(paymentRequest.getId()), backoffBytesPayment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
