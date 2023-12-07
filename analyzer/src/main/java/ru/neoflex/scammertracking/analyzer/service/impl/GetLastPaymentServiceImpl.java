package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.util.ArrayList;
import java.util.List;

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
        List<SavePaymentRequestDto> savePaymentList = new ArrayList<>();
        Flux<SavePaymentRequestDto> savePaymentFlux = Flux.fromIterable(savePaymentList);

        Flux<AggregateGetLastPaymentDto> fluxNonCache = paymentRequestsFlux.filter(val -> {
            if (val.getPaymentResponse() == null) {
                return true;
            }
            return false;
        });

        Flux<AggregateGetLastPaymentDto> fluxCache = paymentRequestsFlux.filter(val -> {
            if (val.getPaymentResponse() != null) {
                return true;
            }
            return false;
        });

        Flux<AggregateGetLastPaymentDto> f = clientService
                .getLastPayment(fluxNonCache)
                .onErrorResume(err -> {
                    HttpStatusCode httpStatusCode = ((WebClientResponseException.NotFound) err).getStatusCode();
                    if (Constants.NOT_FOUND.equals(httpStatusCode.value())) {
                        //savePaymentList.add()
                    }
                    return Mono.empty();
                });

//        checkLastPaymentAsync(fluxCache);
        checkLastPaymentAsync(f);

//        savePaymentRouter.savePayment(savePaymentFlux);

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
}
