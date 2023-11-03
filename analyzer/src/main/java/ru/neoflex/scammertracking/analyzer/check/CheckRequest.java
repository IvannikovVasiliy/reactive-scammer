package ru.neoflex.scammertracking.analyzer.check;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface CheckRequest {
    boolean preCheckSuspicious(PaymentRequestDto paymentRequest);
}
