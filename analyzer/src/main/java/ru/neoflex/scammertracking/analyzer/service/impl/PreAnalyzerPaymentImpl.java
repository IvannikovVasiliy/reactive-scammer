package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;

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
    public void preAnalyzeConsumeMessage(String key, PaymentRequestDto paymentRequest) {
        log.info("received key={} paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                key, paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

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
        }

        lastPaymentService.process(paymentRequest);
    }
}