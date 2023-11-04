package ru.neoflex.scammertracking.analyzer.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PaymentCacheRepositoryImpl implements PaymentCacheRepository {

    private static final String HASH_KEY = "Payment";

    private final ReactiveRedisTemplate<String, PaymentEntity> redisTemplate;
//    private final ReactiveHashOperations<String, String, PaymentEntity> reactiveHashOperations;

    @Override
    public Mono<PaymentEntity> findPaymentByCardNumber(String payerCardNumber) {
        log.info("findPaymentByCardNumber={}", payerCardNumber);
//        Mono<PaymentEntity> p = reactiveHashOperations.get(HASH_KEY, payerCardNumber);
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentEntity.setPayerCardNumber(payerCardNumber);
//        save(paymentEntity);
//        Mono<PaymentEntity> pa = redisTemplate.<String, PaymentEntity>opsForHash().get(HASH_KEY, payerCardNumber);

        Mono<PaymentEntity> payment = redisTemplate
                .<String, PaymentEntity>opsForHash()
                .get(HASH_KEY, payerCardNumber);
        return payment;
//        return redisTemplate.opsForValue().get(payerCardNumber);
    }

    @Override
    public Mono<Boolean> save(PaymentEntity payment) {
        log.info("receive for save. payment={}", payment);
        return redisTemplate.opsForHash().put(HASH_KEY, payment.getPayerCardNumber(), payment);
    }
}
