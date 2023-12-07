package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentRouter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentRouterImpl implements SavePaymentRouter {

    private final ClientService clientService;
    private final PaymentCacheService paymentCacheService;
    private final AtomicBoolean isRedisDropped;
    private final ObjectMapper objectMapper;
    private final PaymentProducer paymentProducer;
    private final Map<Long, WrapPaymentRequestDto> storage;

    public Mono<Void> savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux) {
        clientService
                .savePayment(savePaymentDtoFlux)
                .doOnNext(payment -> {
//                    storage.remove(payment.getId());
//                    if (!isRedisDropped.get()) {
//                        paymentCacheService.saveIfAbsent(payment);
//                    }
                })
//                .retryWhen(Retry
//                        .fixedDelay(2, Duration.ofSeconds(Constants.RETRY_INTERVAL))
//                        .filter(throwable ->
//                                throwable.getCause() instanceof ConnectException ||
//                                        throwable.getCause() instanceof PrematureCloseException)
//                        .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
//                            throw new ConnectionRefusedException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
//                        })))
                .subscribe();

        return Mono.empty();
    }
}
