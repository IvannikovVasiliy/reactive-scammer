package ru.neoflex.scammertracking.analyzer.router;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface RouterPayment {
    void routePayment(boolean isTrusted/*, AtomicBoolean isCacheDeprecated*/, /*PaymentRequestDto*/ SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult);
}
