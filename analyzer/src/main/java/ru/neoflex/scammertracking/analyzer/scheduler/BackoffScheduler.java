package ru.neoflex.scammertracking.analyzer.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class BackoffScheduler {

    private final Map<Long, WrapPaymentRequestDto> storage;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Value("${app.signForHandleBackoffPayment}")
    private Long SIGN_HANDLING_BACKOFF_PAYMENT;

    @Scheduled(fixedRate = Constants.SCHEDULING_ITERATE_BACKOFF_PAYMENTS_INTERVAL)
    public void iterateBackoffPaymentsScheduled() {
        storage.forEach((key, value) -> {
            if (new Date().getTime() - value.getDate() > SIGN_HANDLING_BACKOFF_PAYMENT) {
                byte[] savePaymentByte;
                try {
                    savePaymentByte = objectMapper.writeValueAsBytes(value.getPaymentRequestDto());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                paymentProducer.sendBackoffMessage(value.getPaymentRequestDto().getId().toString(), savePaymentByte);
            }
        });
    }
}
