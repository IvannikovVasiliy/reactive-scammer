package ru.neoflex.scammertracking.analyzer.check.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.neoflex.scammertracking.analyzer.check.PreAnalyzer;
import ru.neoflex.scammertracking.analyzer.check.RequestChecker;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;

@Component
@RequiredArgsConstructor
@Slf4j
public class PreAnalyzerImpl implements PreAnalyzer {

    private final SourceMapperImplementation sourceMapper;
    private final RequestChecker requestChecker;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preAnalyze(PaymentRequestDto paymentRequest) {
        PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
        boolean isPreCheckSuspicious = requestChecker.preCheckSuspicious(paymentRequest);
        if (isPreCheckSuspicious) {
            long key = paymentRequest.getId();
            paymentResult.setTrusted(false);
            byte[] paymentResultBytes = new byte[0];
            try {
                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse paymentResult into bytes");
                throw new RuntimeException(e.getMessage());
            } finally {
                log.error("Sent message with key={} in suspicious-topic", key);
                paymentProducer.sendSuspiciousMessage(String.valueOf(key), paymentResultBytes);
            }
            return false;
        }
        return true;
    }
}
