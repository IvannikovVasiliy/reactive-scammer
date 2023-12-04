package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;
import ru.neoflex.scammertracking.analyzer.service.RouteService;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@EnableScheduling
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(PreAnalyzerPayment preAnalyzerPayment, PaymentProducer paymentProducer, Consumer consumer, RouteService routeService) {
        this.preAnalyzerPayment = preAnalyzerPayment;
        this.paymentProducer = paymentProducer;
        this.consumer = consumer;
        this.routeService = routeService;
    }

    private final PreAnalyzerPayment preAnalyzerPayment;
    private final PaymentProducer paymentProducer;
    private final Consumer<String, byte[]> consumer;
    private final RouteService routeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 500)
    public Mono<Void> pollMessages() {
        log.info("Input schedulling pollMessages");

        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
        byte[] paymentRequestBytes = null;
        String key = null;
        List<PaymentRequestDto> consumeMessages = new ArrayList<>();

        try {
            for (ConsumerRecord<String, byte[]> record : records) {
                paymentRequestBytes = record.value();
                key = record.key();
                PaymentRequestDto paymentRequest = objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                consumeMessages.add(paymentRequest);
                log.info(paymentRequest.toString());
            }
        } catch (IOException e) {
            log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
            paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
        }

        Flux<PaymentRequestDto> flux = Flux.fromIterable(consumeMessages);
        preAnalyzerPayment.preAnalyzeConsumeMessage(flux);

        return Mono.empty();
    }
}
