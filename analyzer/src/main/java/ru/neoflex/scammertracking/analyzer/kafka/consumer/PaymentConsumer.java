package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.service.PaymentPreAnalyzer;

@Service
@Slf4j
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(PaymentPreAnalyzer paymentPreAnalyzer) {
        this.paymentPreAnalyzer = paymentPreAnalyzer;
    }

    private PaymentPreAnalyzer paymentPreAnalyzer;

//    @KafkaListener(topics = "${spring.kafka.topic.payments}", containerFactory = "paymentsKafkaListenerContainerFactory")
//    public void consumePayment(@Payload PaymentRequestDto paymentRequest,
//                               @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
//                               @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timeStamp,
//                               @Header(KafkaHeaders.RECEIVED_KEY) String key) throws Exception {
////        log.info("received key={} paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
////                key, paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());
//
//        log.info("1");
//
////        paymentPreAnalyzer.preAnalyzeConsumeMessage(key, paymentRequest);
//    }

    @KafkaListener(topics = "${spring.kafka.topic.payments}", containerFactory = "paymentsKafkaListenerContainerFactory")
    public void consumePayment(ConsumerRecord<String, PaymentRequestDto> consumerRecord) throws Exception {
        log.info("1");
//        paymentPreAnalyzer.preAnalyzeConsumeMessage(key, paymentRequest);
    }


}
