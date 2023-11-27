package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.ConsumeMessage;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Scheduled(fixedRate = 1500)
    public void pollMessages() {
        log.info("Input schedulling pollMessages");

        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1500));
        byte[] paymentRequestBytes = null;
        String key = null;
        List<Map.Entry<String, PaymentRequestDto>> consumeMessages = new ArrayList<>();

        try {
            for (ConsumerRecord<String, byte[]> record : records) {
                paymentRequestBytes = record.value();
                key = record.key();
                PaymentRequestDto paymentRequest = objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                consumeMessages.add(Map.entry(record.key(), paymentRequest));
            }
            preAnalyzerPayment.preAnalyzeConsumeMessage(consumeMessages);
        } catch (IOException e) {
            log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
            paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
        }
    }
}
