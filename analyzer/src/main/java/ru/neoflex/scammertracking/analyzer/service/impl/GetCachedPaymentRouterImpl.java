package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.neoflex.scammertracking.analyzer.check.PreAnalyzer;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCachedPaymentRouterImpl implements GetCachedPaymentRouter {

    private final GetLastPaymentService lastPaymentService;
    private final SourceMapperImplementation sourceMapper;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PreAnalyzer preAnalyzer;

    @Override
    public Mono<Void> preAnalyzeConsumeMessage(Flux<PaymentRequestDto> consumeMessages) {
        Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux = consumeMessages
                .flatMap(paymentRequest -> {
                    log.info("flatMap preAnalyzeConsumeMessage. paymentRequest with id={}", paymentRequest.getId());

                    boolean isTrusted = preAnalyzer.preAnalyze(paymentRequest);
                    if (!isTrusted) {
                        throw new RuntimeException(String.format("Payment with id=%d is failed in a stage of pre-analyze", paymentRequest.getId()));
                    }

                    AggregateGetLastPaymentDto analyzeModel = new AggregateGetLastPaymentDto();
                    analyzeModel.setPaymentRequest(paymentRequest);

                    paymentCacheRepository
                            .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                            .doOnNext(payment -> {
                                log.info("get last payment with id={} from cache", payment.getIdPayment());
                                LastPaymentResponseDto lastPaymentResponse = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                                analyzeModel.setPaymentResponse(lastPaymentResponse);
                            })
//                            .subscribeOn(Schedulers.newBoundedElastic(1, 1, "m"))
                            .subscribe();

                    return Mono
                            .just(analyzeModel);
//                            .delayElement(Duration.ofMillis(Constants.DELAY_REDIS_RESPONSE_MILLIS));

                })
                .onErrorResume(err ->
                        Mono.empty());

        lastPaymentService.process(aggregatePaymentsFlux);

        return Mono.empty();
    }
}