package ru.neoflex.scammertracking.analyzer.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class BackoffScheduler {

    private final Map<Long, WrapPaymentRequestDto> storage;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 10_000)
    public Mono<Void> scheduled() {
        storage.forEach((key, value) -> {
            if (new Date().getTime() - value.getDate() > 10_000) {
                byte[] savePaymentByte;
                try {
                    savePaymentByte = objectMapper.writeValueAsBytes(value.getPaymentRequestDto());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                paymentProducer.sendBackoffMessage(value.getPaymentRequestDto().getId().toString(), savePaymentByte);
            }
        });

        return Mono.empty();
    }
}
