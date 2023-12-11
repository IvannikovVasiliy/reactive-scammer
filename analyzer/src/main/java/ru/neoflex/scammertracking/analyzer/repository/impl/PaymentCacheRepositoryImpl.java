package ru.neoflex.scammertracking.analyzer.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PaymentCacheRepositoryImpl implements PaymentCacheRepository {

    private static final String HASH_KEY = "Payment";
    private static final Long EXPIRATION_PAYMENT = 30L;

    private final ReactiveRedisTemplate<String, PaymentEntity> redisTemplate;

    @Override
    public Mono<PaymentEntity> findPaymentByCardNumber(String payerCardNumber) {
        log.info("findPaymentByCardNumber. payerCardNumber={}", payerCardNumber);

        return redisTemplate
                .<String, PaymentEntity>opsForHash()
                .get(HASH_KEY, payerCardNumber);
    }

    @Override
    public Mono<Boolean> save(PaymentEntity payment) {
        log.info("receive for save. payment={}", payment);
        return redisTemplate.opsForHash().put(HASH_KEY, payment.getPayerCardNumber(), payment);
    }

    @Override
    public Mono<Boolean> expire() {
        log.info("set expiration for payment cache");
        return redisTemplate.expire(HASH_KEY, Duration.of(EXPIRATION_PAYMENT, ChronoUnit.SECONDS));
    }
}
