package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.PrematureCloseException;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.exception.ConnectionRefusedException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;

import java.net.ConnectException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentRouterImpl implements SavePaymentRouter {

    private final ClientService clientService;
    private final PaymentCacheService paymentCacheService;
    private final AtomicBoolean isRedisDropped;
    private final PaymentProducer paymentProducer;
    private final Map<Long, WrapPaymentRequestDto> storage;

    @Value("${app.retryCount}")
    private Long RETRY_COUNT;
    @Value("${app.retryIntervalSeconds}")
    private Long RETRY_INTERVAL_SECONDS;

    public void savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux) {
        clientService
                .savePayment(savePaymentDtoFlux)
                .doOnNext(payment -> {
                    storage.remove(payment.getId());
                    if (!isRedisDropped.get()) {
                        paymentCacheService.saveIfAbsent(payment);
                    }
                    //paymentProducer.sendCheckedMessage(payment);
                })
//                .retryWhen(Retry
//                        .fixedDelay(RETRY_COUNT, Duration.ofSeconds(RETRY_INTERVAL_SECONDS))
//                        .filter(throwable ->
//                                throwable.getCause() instanceof ConnectException ||
//                                        throwable.getCause() instanceof PrematureCloseException)
//                        .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
//                            throw new ConnectionRefusedException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
//                        })))
                .subscribe(new BaseSubscriber<SavePaymentResponseDto>() {

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        super.hookOnError(throwable);
                    }
                });
    }
}
