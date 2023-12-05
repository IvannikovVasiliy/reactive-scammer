package ru.neoflex.scammertracking.analyzer.repository;

import org.springframework.data.redis.core.ReactiveHashOperations;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

public interface PaymentCacheRepository {
    Mono<PaymentEntity> findPaymentByCardNumber(String payerCardNumber);
    Mono<Boolean> save(PaymentEntity payment);
    Mono<Boolean> expire();
}