package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.PreAnalyzer;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCachedPaymentRouterImpl implements GetCachedPaymentRouter {

    private final GetLastPaymentService lastPaymentService;
    private final SourceMapperImplementation sourceMapper;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PreAnalyzer preAnalyzer;
    private final PaymentCacheService paymentCacheService;

    @Override
    public Mono<Void> preAnalyzeConsumeMessage(Flux<PaymentRequestDto> consumeMessages) {
        Flux<AggregateLastPaymentDto> paymentsFlux = consumeMessages
                .flatMap(paymentRequest -> {
                    log.info("flatMap preAnalyzeConsumeMessage. paymentRequest with id={}", paymentRequest.getId());

                    boolean isTrusted = preAnalyzer.preAnalyze(paymentRequest);
                    if (!isTrusted) {
                        throw new RuntimeException(String.format("Payment with id=%d is failed in a stage of pre-analyze", paymentRequest.getId()));
                    }

                    AggregateLastPaymentDto analyzeModel = new AggregateLastPaymentDto();
                    analyzeModel.setPaymentRequest(paymentRequest);

                    paymentCacheRepository
                            .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                            .doOnNext(payment -> {
                                LastPaymentResponseDto lastPaymentResponse = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                                analyzeModel.setPaymentResponse(lastPaymentResponse);
                            })
                            .subscribe();

                    return Mono.just(analyzeModel).delayElement(Duration.ofMillis(5));

                })
                .onErrorResume(err ->
                        Mono.empty());

        lastPaymentService.process(paymentsFlux);

        return Mono.empty();
    }
}