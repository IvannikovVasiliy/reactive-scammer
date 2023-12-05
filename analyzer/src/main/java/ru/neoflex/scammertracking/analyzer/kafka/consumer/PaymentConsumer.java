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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.GetCachedPaymentRouter;
import ru.neoflex.scammertracking.analyzer.util.ConfigUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@EnableScheduling
public class PaymentConsumer {

    @Autowired
    public PaymentConsumer(GetCachedPaymentRouter getCachedPaymentRouter,
                           PaymentProducer paymentProducer,
                           Consumer<String, byte[]> consumer,
                           ObjectMapper objectMapper) {
        this.getCachedPaymentRouter = getCachedPaymentRouter;
        this.paymentProducer = paymentProducer;
        this.consumer = consumer;
        this.objectMapper = objectMapper;
    }

    private final GetCachedPaymentRouter getCachedPaymentRouter;
    private final PaymentProducer paymentProducer;
    private final Consumer<String, byte[]> consumer;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 500)
    public void pollMessages() {
        log.info("Input schedulling pollMessages");

        List<PaymentRequestDto> consumeMessages = new ArrayList<>();

        Mono
                .fromRunnable(() -> {
                    final long consumerPollDurationMillis = ConfigUtil.getConsumerPollDurationMillis();
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(consumerPollDurationMillis));
                    byte[] paymentRequestBytes = null;
                    String key = null;
                    try {
                        for (ConsumerRecord<String, byte[]> record : records) {
                            paymentRequestBytes = record.value();
                            key = record.key();
                            PaymentRequestDto paymentRequest =
                                    objectMapper.readValue(paymentRequestBytes, PaymentRequestDto.class);
                            consumeMessages.add(paymentRequest);
                            log.info(paymentRequest.toString());
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
    }
}
