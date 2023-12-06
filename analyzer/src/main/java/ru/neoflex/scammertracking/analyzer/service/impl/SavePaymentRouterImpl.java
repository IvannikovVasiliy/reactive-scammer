package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentRouterImpl implements SavePaymentRouter {

    private final ClientService clientService;
    private final PaymentCacheService paymentCacheService;
    private final AtomicBoolean isRedisDropped;

    public Mono<Void> savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux) {

        clientService
                .savePayment(savePaymentDtoFlux)
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnNext(SavePaymentResponseDto value) {
                        super.hookOnNext(value);
                        if (!isRedisDropped.get()) {
                            paymentCacheService.saveIfAbsent(value);
                        }
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

        return Mono.empty();
    }
}
