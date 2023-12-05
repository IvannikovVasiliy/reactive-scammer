package ru.neoflex.scammertracking.analyzer.check;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface PreAnalyzer {
    boolean preAnalyze(PaymentRequestDto paymentRequest);
}
