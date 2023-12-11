package ru.neoflex.scammertracking.analyzer.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.BaseSubscriber;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class RedisScheduler {

    private final PaymentCacheRepository paymentCacheRepository;
    private final AtomicBoolean isRedisDropped;

    @Scheduled(fixedRate = Constants.SCHEDULING_EXPIRATION_CACHE_INTERVAL)
    public void setExpirePaymentSchedule() {
        paymentCacheRepository.expire().subscribe();
    }

    @Scheduled(fixedRate = Constants.SCHEDULING_REDIS_START)
    public void redisStartSchedule() {
        paymentCacheRepository.expire().subscribe(new BaseSubscriber<>() {
            @Override
            protected void hookOnComplete() {
                super.hookOnComplete();
                isRedisDropped.set(false);
            }
        });
    }
}
