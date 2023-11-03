package ru.neoflex.scammertracking.analyzer.geo;

import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface GeoAnalyzer {
    boolean checkPayment(LastPaymentResponseDto lastPayment, PaymentRequestDto currentPayment);
}
