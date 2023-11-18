package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

import java.io.IOException;

@Service
@Slf4j
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(PreAnalyzerPayment preAnalyzerPayment, PaymentProducer paymentProducer) {
        this.preAnalyzerPayment = preAnalyzerPayment;
        this.paymentProducer = paymentProducer;
    }

    private final PreAnalyzerPayment preAnalyzerPayment;
    private final PaymentProducer paymentProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "${spring.kafka.topic.payments}", containerFactory = "paymentsKafkaListenerContainerFactory")
    public void consumePayment(@Payload byte[] paymentRequestBytes,
                               @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Input consumePayment. Received key={} bytes array", key);

        try {
            PaymentRequestDto paymentRequest = objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
            preAnalyzerPayment.preAnalyzeConsumeMessage(key, paymentRequest);
        } catch (IOException e) {
            log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
            paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
        }
    }
}
