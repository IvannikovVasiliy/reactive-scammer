package ru.neoflex.scammertracking.analyzer.check.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CheckRequestImpl implements CheckRequest {

    public boolean preCheckSuspicious(PaymentRequestDto paymentRequest) {
        log.info("Check suspicious. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

        if (paymentRequest.getPayerCardNumber().length() < 6) {
            log.warn("Result validating. The message is suspicious, because the length of payerCardNumber is too short");
            return true;
        }
        if (paymentRequest.getReceiverCardNumber().length() < 6) {
            log.warn("Result validating. The message is suspicious, because the length of receiverCardNumber is too short");
            return true;
        }
        if (LocalDateTime.now().isBefore(paymentRequest.getDate())) {
            log.warn("Result validating. The message is suspicious, because date of paymentRequest more than current datetime");
            return true;
        }

        log.info("Result validating. The message is valid");
        return false;
    }
}
