package ru.neoflex.scammertracking.analyzer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

@Slf4j
public class CustomLogs {

    public static void logCreateTopicPayments() {
        log.debug("create topic payments with partitions-count={} and replicas-count={}",
                ConfigUtil.getCountPartitionsTopicPayments(), ConfigUtil.getCountReplicasTopicPayments());
    }

    public static void logCreateTopicCheckedPayments() {
        log.debug("create topic checked-payments with partitions-count={} and replicas-count={}",
                ConfigUtil.getCountPartitionsTopicCheckedPayments(), ConfigUtil.getCountReplicasTopicCheckedPayments());
    }

    public static void logCreateTopicSuspiciousPayments() {
        log.debug("create topic suspicious-payments with partitions-count={} and replicas-count={}",
                ConfigUtil.getCountPartitionsTopicSuspiciousPayments(), ConfigUtil.getCountReplicasTopicSuspiciousPayments());
    }

    public static void logPreCheckSuspicious(PaymentRequestDto paymentRequest) {
        log.info("Check suspicious. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());
    }

    public static void logErrorSendCheckedMessage(PaymentResponseDto payment, Throwable exception) {
        log.error("error. Unable to send message with key={} message={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } due to : {}",
                payment.getId(), payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate(), exception.getMessage());
    }

    public static void logSendCheckedMessage(PaymentResponseDto payment, SendResult<String, PaymentResponseDto> result) {
        log.info("Sent message={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } with offset=={} in checked-payments topic",
                payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate(), result.getRecordMetadata().offset());
    }
}
