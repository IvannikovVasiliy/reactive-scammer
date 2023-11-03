package ru.neoflex.scammertracking.analyzer.repository;

import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

public interface PaymentCacheRepository {
    Mono<PaymentEntity> findPaymentByCardNumber(String payerCardNumber);
    Mono<Boolean> save(PaymentEntity payment);
}
