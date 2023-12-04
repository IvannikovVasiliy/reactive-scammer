package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreAnalyzerPaymentImpl implements PreAnalyzerPayment {

    private final GetLastPaymentService lastPaymentService;
    private final PaymentProducer paymentProducer;
    private final SourceMapperImplementation sourceMapper;
    private final CheckRequest checkRequest;
    private final ObjectMapper objectMapper;
    private final PaymentCacheRepository paymentCacheRepository;


    @Override
    public Mono<Void> preAnalyzeConsumeMessage(Flux<PaymentRequestDto> consumeMessages) {
        log.info("Input preAnalyzeConsumeMessage. received list of consumeMessages");

        Flux<AggregateLastPaymentDto> paymentsFlux = consumeMessages
                .flatMap(paymentRequest -> {

                    preAnalyze(paymentRequest);

                    LastPaymentResponseDto lastPaymentResponseDto = new LastPaymentResponseDto();
                    AggregateLastPaymentDto analyzeModel = new AggregateLastPaymentDto();
                    analyzeModel.setPaymentRequest(paymentRequest);

                    paymentCacheRepository
                            .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                            .doOnNext(payment -> {
                                LastPaymentResponseDto lastPaymentResponse = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                                analyzeModel.setPaymentResponse(lastPaymentResponse);
                            })
                            .subscribe();

                    return Mono.just(analyzeModel).delayElement(Duration.ofMillis(5));

                })
                .onErrorResume(err -> Mono.empty());

        lastPaymentService.process(paymentsFlux);

        return Mono.empty();
    }

    private void preAnalyze(PaymentRequestDto paymentRequest) {
        PaymentResponseDto paymentResult =
                sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
        boolean isPreCheckSuspicious = checkRequest.preCheckSuspicious(paymentRequest);
        if (isPreCheckSuspicious) {
            long key = paymentRequest.getId();
            log.error("response. Sent message with key={} in suspicious-topic", key);
            paymentResult.setTrusted(false);
            byte[] paymentResultBytes = new byte[0];
            try {
                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse paymentResult into bytes");
                throw new RuntimeException(e.getMessage());
            } finally {
                paymentProducer.sendSuspiciousMessage(String.valueOf(key), paymentResultBytes);
            }
        }
    }
//        for (var consumeMessage : consumeMessages) {
//            String key = consumeMessage.getKey();
//            PaymentRequestDto paymentRequest = consumeMessage.getValue();
//            PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
//
//            boolean isPreCheckSuspicious = checkRequest.preCheckSuspicious(paymentRequest);
//            if (isPreCheckSuspicious) {
//                log.error("response. Sent message with key={} in suspicious-topic", key);
//                paymentResult.setTrusted(false);
//                byte[] paymentResultBytes = new byte[0];
//                try {
//                    paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
//                } catch (JsonProcessingException e) {
//                    log.error("Unable to parse paymentResult into bytes");
//                } finally {
//                    paymentProducer.sendSuspiciousMessage(key, paymentResultBytes);
//                }
//                return;
//            } else {
//                paymentRequests.add(paymentRequest);
//            }
//        }
//
//        lastPaymentService.process(paymentRequests);
}