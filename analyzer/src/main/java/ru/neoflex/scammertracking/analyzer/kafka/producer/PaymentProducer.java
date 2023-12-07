package ru.neoflex.scammertracking.analyzer.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentProducer {

    @Autowired
    public PaymentProducer(KafkaTemplate<String, PaymentResponseDto> kafkaJsonTemplate,
                           KafkaTemplate<String, byte[]> kafkaBytesTemplate) {
        this.kafkaJsonTemplate = kafkaJsonTemplate;
        this.kafkaBytesTemplate = kafkaBytesTemplate;
    }

    private final KafkaTemplate<String, PaymentResponseDto> kafkaJsonTemplate;
    private final KafkaTemplate<String, byte[]> kafkaBytesTemplate;

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
                log.error("error. Unable to send message with key={} message={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } due to : {}",
                        payment.getId(), payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate(), exception.getMessage());
            } else {
                log.info("Sent message={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } with offset=={} in checked-payments topic",
                        payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate(), result.getRecordMetadata().offset());
            }
        });
    }

    public void sendSuspiciousMessage(String key, byte[] payment) {
        log.info("Input sendSuspiciousMessage. Received key={} and bytes array", key);

        CompletableFuture<SendResult<String, byte[]>> future = kafkaBytesTemplate.send(suspiciousPaymentsTopic, key, payment);
        future.whenCompleteAsync((result, exception) -> {
            if (null != exception) {
                log.error("error. Unable to send bytes-array message with key={} due to : {}",
                        key, exception.getMessage());
            } else {
                log.info("Sent message with key={} and offset=={} in {} ",
                        key, result.getRecordMetadata().offset(), suspiciousPaymentsTopic);
            }
        });
    }

    public void sendBackoffMessage(String key, byte[] payment) {
        log.info("Input sendBackoffMessage. Received key={} and bytes array", key);

        CompletableFuture<SendResult<String, byte[]>> future = kafkaBytesTemplate.send(backoffPaymentsTopic, key, payment);
        future.whenCompleteAsync((result, exception) -> {
            if (null != exception) {
                log.error("error. Unable to send bytes-array message with key={} in {} due to : {}",
                        key, backoffPaymentsTopic, exception.getMessage());
            } else {
                log.info("Sent message with key={} and offset=={} in {} ",
                        key, result.getRecordMetadata().offset(), backoffPaymentsTopic);
            }
        });
    }
}
