package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.PreAnalyzer;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.SuspiciousPaymentException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentRouter;

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
    private final ObjectMapper objectMapper;

    @Override
    public void preAnalyzeConsumeMessage(Flux<PaymentRequestDto> paymentRequestDtoFlux) {
        Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux = paymentRequestDtoFlux
                .flatMap(paymentRequest -> {
                    log.info("flatMap preAnalyzeConsumeMessage. paymentRequest with id={}", paymentRequest.getId());

                    boolean isTrusted = preAnalyzer.preAnalyze(paymentRequest);
                    if (!isTrusted) {
                        byte[] suspiciousPaymentBytes;
                        try {
                            suspiciousPaymentBytes = objectMapper.writeValueAsBytes(paymentRequest);
                        } catch (JsonProcessingException e) {
                            return Flux.error(new RuntimeException(e));
                        }

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
                                    if (err instanceof RedisConnectionFailureException) {
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