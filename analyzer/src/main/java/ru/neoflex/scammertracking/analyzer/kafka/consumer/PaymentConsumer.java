package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

import java.io.IOException;
import java.time.Duration;

@Service
@Slf4j
@EnableScheduling
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(PreAnalyzerPayment preAnalyzerPayment, PaymentProducer paymentProducer, Consumer consumer) {
        this.preAnalyzerPayment = preAnalyzerPayment;
        this.paymentProducer = paymentProducer;
        this.consumer = consumer;
    }

    private final PreAnalyzerPayment preAnalyzerPayment;
    private final PaymentProducer paymentProducer;
    private final Consumer<String, byte[]> consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //    @KafkaListener(topics = "${spring.kafka.topic.payments}", containerFactory = "paymentsKafkaListenerContainerFactory")
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

    @Scheduled(fixedRate = 1000)
    public void method() {
        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
        byte[] paymentRequestBytes = null;
        String key = null;

        try {
            for (ConsumerRecord<String, byte[]> record : records) {
                paymentRequestBytes = record.value();
                key = record.key();
                PaymentRequestDto paymentRequest = objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                preAnalyzerPayment.preAnalyzeConsumeMessage(key, paymentRequest);
            }
        } catch (IOException e) {
            log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
            paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
        }

//        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofSeconds(1));
//        for (ConsumerRecord<String, byte[]> rec : records) {
//            System.out.println(rec.value());
//        }
//        System.out.println("------------");

        // close
    }
}
