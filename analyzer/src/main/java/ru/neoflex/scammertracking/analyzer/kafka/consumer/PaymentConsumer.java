package ru.neoflex.scammertracking.analyzer.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.WrapPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.router.GetCachedPaymentRouter;
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
    public PaymentConsumer(GetCachedPaymentRouter cachePaymentRouter,
                           PaymentProducer paymentProducer,
                           @Qualifier("createConsumer") Consumer<String, byte[]> consumer,
                           @Qualifier("createBackoffConsumer") Consumer<String, byte[]> backoffConsumer,
                           ObjectMapper objectMapper,
                           @Qualifier("storage") Map<Long, WrapPaymentRequestDto> storage) {
        this.cachePaymentRouter = cachePaymentRouter;
        this.paymentProducer = paymentProducer;
        this.consumer = consumer;
        this.backoffConsumer = backoffConsumer;
        this.objectMapper = objectMapper;
        this.storage = storage;
    }

    private final GetCachedPaymentRouter cachePaymentRouter;
    private final PaymentProducer paymentProducer;
    private final Consumer<String, byte[]> consumer;
    private final Consumer<String, byte[]> backoffConsumer;
    private final ObjectMapper objectMapper;
    private final Map<Long, WrapPaymentRequestDto> storage;

    @Value("${app.durationPollMillis}")
    private Long DURATION_POLL_MESSAGES_MILLIS;

    @Scheduled(fixedRate = Constants.SCHEDULING_POLL_MESSAGES_INTERVAL)
    public void pollMessages() {
        log.info("Start pollMessages in scheduling");

        List<PaymentRequestDto> consumeMessagesList = new ArrayList<>();
        Mono
                .fromRunnable(() -> {
                    ConsumerRecords<String, byte[]> records =
                            consumer.poll(Duration.ofMillis(DURATION_POLL_MESSAGES_MILLIS));
                    byte[] paymentRequestBytes = null;
                    String key = null;
                    try {
                        for (ConsumerRecord<String, byte[]> record : records) {
                            paymentRequestBytes = record.value();
                            key = record.key();
                            PaymentRequestDto paymentRequest =
                                    objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                            consumeMessagesList.add(paymentRequest);
                            storage.putIfAbsent(paymentRequest.getId(), new WrapPaymentRequestDto(paymentRequest, new Date().getTime()));
                        }
                    } catch (IOException e) {
                        log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
                        paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
                    }
                })
                .doOnSuccess(val -> {
                        Flux<PaymentRequestDto> paymentFlux = Flux.fromIterable(consumeMessagesList);
                        cachePaymentRouter.preAnalyzeConsumeMessage(paymentFlux);
                })
                .subscribe();
    }

    @Scheduled(fixedRate = Constants.SCHEDULING_POLL_MESSAGES_INTERVAL)
    public void pollBackoffMessages() {
        log.info("Start pollBackoffMessages in scheduling");

        List<PaymentRequestDto> consumeMessagesList = new ArrayList<>();
        Mono
                .fromRunnable(() -> {
                    ConsumerRecords<String, byte[]> records =
                            backoffConsumer.poll(Duration.ofMillis(DURATION_POLL_MESSAGES_MILLIS));
                    byte[] paymentRequestBytes = null;
                    String key = null;
                        try {
                            for (ConsumerRecord<String, byte[]> record : records) {
                            paymentRequestBytes = record.value();
                            key = record.key();
                            PaymentRequestDto paymentRequest =
                                    objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                            consumeMessagesList.add(paymentRequest);
                        }
                    } catch (IOException e) {
                        log.error("Cannot map input request={} to PaymentRequestDto.class", paymentRequestBytes);
                        paymentProducer.sendSuspiciousMessage(key, paymentRequestBytes);
                    }
                })
                .doOnSuccess(val -> {
                    Flux<PaymentRequestDto> paymentFlux = Flux.fromIterable(consumeMessagesList);
                    cachePaymentRouter.preAnalyzeConsumeMessage(paymentFlux);
                })
                .subscribe();
    }
}
