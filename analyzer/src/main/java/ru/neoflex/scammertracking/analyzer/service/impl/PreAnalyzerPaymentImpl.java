package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.ConsumeMessage;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PreAnalyzerPaymentImpl implements PreAnalyzerPayment {

    private final GetLastPaymentService lastPaymentService;
    private final PaymentProducer paymentProducer;
    private final SourceMapperImplementation sourceMapper;
    private final CheckRequest checkRequest;
    private final ObjectMapper objectMapper;

    @Override
    public void preAnalyzeConsumeMessage(List<Map.Entry<String, PaymentRequestDto>> consumeMessages) {
        log.info("Input preAnalyzeConsumeMessage. received list of consumeMessages");

        List<PaymentRequestDto> paymentRequests = new ArrayList<>();

        for (var consumeMessage : consumeMessages) {
            String key = consumeMessage.getKey();
            PaymentRequestDto paymentRequest = consumeMessage.getValue();
            PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);

            boolean isPreCheckSuspicious = checkRequest.preCheckSuspicious(paymentRequest);
            if (isPreCheckSuspicious) {
                log.error("response. Sent message with key={} in suspicious-topic", key);
                paymentResult.setTrusted(false);
                byte[] paymentResultBytes = new byte[0];
                try {
                    paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
                } catch (JsonProcessingException e) {
                    log.error("Unable to parse paymentResult into bytes");
                } finally {
                    paymentProducer.sendSuspiciousMessage(key, paymentResultBytes);
                }
                return;
            } else {
                paymentRequests.add(paymentRequest);
            }
        }

        lastPaymentService.process(paymentRequests);
    }
}