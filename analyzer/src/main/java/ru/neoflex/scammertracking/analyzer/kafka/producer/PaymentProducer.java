package ru.neoflex.scammertracking.analyzer.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.util.CustomLogs;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentProducer {

    @Autowired
    public PaymentProducer(KafkaTemplate<String, PaymentResponseDto> kafkaJsonTemplate,
                           KafkaTemplate<String, byte[]> kafkaBytesTemplate,
                           Map<Long, WrapPaymentRequestDto> storage) {
        this.kafkaJsonTemplate = kafkaJsonTemplate;
        this.kafkaBytesTemplate = kafkaBytesTemplate;
        this.storage = storage;
    }

    private final KafkaTemplate<String, PaymentResponseDto> kafkaJsonTemplate;
    private final KafkaTemplate<String, byte[]> kafkaBytesTemplate;
    private final Map<Long, WrapPaymentRequestDto> storage;

    @Value("${spring.kafka.topic.suspicious-payments}")
    private String suspiciousPaymentsTopic;
    @Value("${spring.kafka.topic.checked-payments}")
    private String checkedPaymentsTopic;
    @Value("${spring.kafka.topic.backoff-payments}")
    private String backoffPaymentsTopic;

    public void sendCheckedMessage(PaymentResponseDto payment) {
        CompletableFuture<SendResult<String, PaymentResponseDto>> future = kafkaJsonTemplate.send(checkedPaymentsTopic, String.valueOf(payment.getId()), payment);

        future.whenCompleteAsync((result, exception) -> {
            if (null != exception) {
                CustomLogs.logErrorSendCheckedMessage(payment, exception);
            } else {
                CustomLogs.logSendCheckedMessage(payment, result);
            }
        });
    }

    public void sendSuspiciousMessage(String key, byte[] payment) {
        log.info("Input sendSuspiciousMessage. Received key={} and bytes array", key);

        CompletableFuture<SendResult<String, byte[]>> future = kafkaBytesTemplate.send(suspiciousPaymentsTopic, key, payment);
        future.whenCompleteAsync((result, exception) -> {
            if (null != exception) {
                log.error("error. Unable to send bytes-array message with key={} due to : {}", key, exception.getMessage());
            } else {
                log.info("Sent message with key={} and offset=={} in {} ", key, result.getRecordMetadata().offset(), suspiciousPaymentsTopic);
                storage.remove(Long.valueOf(key));
            }
        });
    }

    public void sendBackoffMessage(String key, byte[] payment) {
        log.info("Input sendBackoffMessage. Received key={} and bytes array", key);

        CompletableFuture<SendResult<String, byte[]>> future = kafkaBytesTemplate.send(backoffPaymentsTopic, key, payment);
        future.whenCompleteAsync((result, exception) -> {
            if (null != exception) {
                log.error("error. Unable to send bytes-array message with key={} in {} due to : {}", key, backoffPaymentsTopic, exception.getMessage());
            } else {
                storage.remove(Long.valueOf(key));
                log.info("Sent message with key={} and offset=={} in {} ", key, result.getRecordMetadata().offset(), backoffPaymentsTopic);
            }
        });
    }
}
