package ru.neoflex.scammertracking.analyzer.check;

import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface PaymentChecker {
    boolean preCheckSuspicious(PaymentRequestDto paymentRequest);

    void checkLastPayment(Flux<AggregateGetLastPaymentDto> aggregatePaymentsFlux);
}
