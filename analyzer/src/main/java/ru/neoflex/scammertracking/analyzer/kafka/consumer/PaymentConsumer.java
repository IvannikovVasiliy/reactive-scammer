package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@EnableScheduling
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(GetCachedPaymentRouter getCachedPaymentRouter,
                           PaymentProducer paymentProducer,
                           Consumer<String, byte[]> consumer,
                           ObjectMapper objectMapper,
                           Map<Long, WrapPaymentRequestDto> storage) {
        this.getCachedPaymentRouter = getCachedPaymentRouter;
        this.paymentProducer = paymentProducer;
        this.consumer = consumer;
        this.objectMapper = objectMapper;
        this.storage = storage;
    }

    private final GetCachedPaymentRouter getCachedPaymentRouter;
    private final PaymentProducer paymentProducer;
    private final Consumer<String, byte[]> consumer;
    private final ObjectMapper objectMapper;
    private final Map<Long, WrapPaymentRequestDto> storage;

    @Value("${app.durationPollMillis}")
    private Long durationPollMillis;

    @Scheduled(fixedRate = 500)
    public Mono<Void> pollMessages() {
        log.info("Start pollMessages in scheduling");

        List<PaymentRequestDto> consumeMessages = new ArrayList<>();
        Mono
                .fromRunnable(() -> {
                    ConsumerRecords<String, byte[]> records =
                            consumer.poll(Duration.ofMillis(durationPollMillis));
                    byte[] paymentRequestBytes = null;
                    String key = null;
                    try {
                        for (ConsumerRecord<String, byte[]> record : records) {
                            paymentRequestBytes = record.value();
                            key = record.key();
                            PaymentRequestDto paymentRequest =
                                    objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                            consumeMessages.add(paymentRequest);
                            storage.put(paymentRequest.getId(), new WrapPaymentRequestDto(paymentRequest, new Date().getTime()));
                            System.out.println();
                        }
                    } catch (IOException e) {
                        log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
                        paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
                    }
                })
                .doOnSuccess(val -> {
                    Flux<PaymentRequestDto> flux = Flux.fromIterable(consumeMessages);
                    getCachedPaymentRouter.preAnalyzeConsumeMessage(flux);
                })
                .subscribe();

        return Mono.empty();
    }
}
