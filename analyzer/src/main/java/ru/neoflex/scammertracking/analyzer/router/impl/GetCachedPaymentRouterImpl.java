package ru.neoflex.scammertracking.analyzer.router.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.PreAnalyzer;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.SuspiciousPaymentException;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.router.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.router.GetLastPaymentRouter;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCachedPaymentRouterImpl implements GetCachedPaymentRouter {

    private final GetLastPaymentRouter lastPaymentService;
    private final SourceMapperImplementation sourceMapper;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PreAnalyzer preAnalyzer;
    private final AtomicBoolean isRedisDropped;

    @Override
    public void preAnalyzeConsumeMessage(Flux<PaymentRequestDto> paymentRequestDtoFlux) {
        Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux = paymentRequestDtoFlux
                .flatMap(paymentRequest -> {
                    log.info("flatMap preAnalyzeConsumeMessage. paymentRequest with id={}", paymentRequest.getId());

                    boolean isTrusted = preAnalyzer.preAnalyze(paymentRequest);
                    if (!isTrusted) {
                        return Flux.error(new SuspiciousPaymentException(String.format("Payment with id=%d is failed in a stage of pre-analyze", paymentRequest.getId())));
                    }

                    AggregateGetLastPaymentDto analyzeModel = new AggregateGetLastPaymentDto();
                    analyzeModel.setPaymentRequest(paymentRequest);

                    if (!isRedisDropped.get()) {
                        return paymentCacheRepository
                                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                                .doOnNext(payment -> {
                                    log.info("get last payment with id={} from cache", payment.getIdPayment());
                                    LastPaymentResponseDto lastPaymentResponse = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                                    analyzeModel.setPaymentResponse(lastPaymentResponse);
                                })
                                .onErrorResume(err -> {
                                    if (err instanceof DataAccessException) {
                                        log.warn("Connection refused for Redis");
                                        isRedisDropped.set(true);
                                        return Mono.empty();
                                    }
                                    return Mono.error(err);
                                })
                                .then(Mono.just(analyzeModel));
                    } else {
                        log.warn("Connection refused for Redis");
                        return Mono.just(analyzeModel);
                    }
                })
                .onErrorResume(err ->
                        Mono.empty());

        lastPaymentService.process(aggregatePaymentsFlux);
    }
}