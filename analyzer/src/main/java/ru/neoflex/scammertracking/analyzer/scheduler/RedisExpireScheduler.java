package ru.neoflex.scammertracking.analyzer.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.util.Constants;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class RedisExpireScheduler {

    private final PaymentCacheRepository paymentCacheRepository;

    @Scheduled(fixedRate = Constants.SCHEDULING_ITERATE_BACKOFF_PAYMENTS_INTERVAL)
    public void setExpirePaymentSchedule() {
        paymentCacheRepository.expire().subscribe();
    }
}
