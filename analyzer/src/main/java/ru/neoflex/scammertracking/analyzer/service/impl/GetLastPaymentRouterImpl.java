package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentRouter;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLastPaymentRouterImpl implements GetLastPaymentRouter {

    private final SourceMapperImplementation sourceMapper;
    private final ClientService clientService;
    private final GeoAnalyzer geoAnalyzer;
    private final SavePaymentRouter savePaymentRouter;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Flux<AggregateGetLastPaymentDto> paymentRequestsFlux) {
        List<AggregateGetLastPaymentDto> listCache = new ArrayList<>();
        List<AggregateGetLastPaymentDto> listNonCache = new ArrayList<>();

        Flux<AggregateGetLastPaymentDto> fluxNonCache = Flux.fromIterable(listNonCache);
        Flux<AggregateGetLastPaymentDto> fluxCache = Flux.fromIterable(listCache);
        paymentRequestsFlux
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnNext(AggregateGetLastPaymentDto val) {
                        super.hookOnNext(val);
                        if (val.getPaymentResponse() == null) {
                            listNonCache.add(val);
                        } else {
                            listCache.add(val);
                        }

                    }
                    
                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        Flux<AggregateGetLastPaymentDto> f = clientService
                                .getLastPayment(fluxNonCache);

                        checkLastPaymentAsync(f);
                        checkLastPaymentAsync(fluxCache);
                    }
                });

    }

    private void checkLastPaymentAsync(Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux) {
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
                            return Flux.error(new RuntimeException("Payment with id={} is suspicious, because it is failed in geo-analyzing-stage"));
                        }
                    }

                    PaymentResponseDto paymentResponseDto = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(value.getPaymentRequest());
                    SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(value.getPaymentRequest());
                    SavePaymentDto savePaymentDto = new SavePaymentDto(savePaymentRequestDto, paymentResponseDto);

                    return Mono.just(savePaymentDto.getSavePaymentRequestDto());
                })
                .onErrorResume(err ->
                        Mono.empty());

        savePaymentRouter.savePayment(savePaymentFlux);
    }
}
