package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface GetCachedPaymentRouter {
    void preAnalyzeConsumeMessage(Flux<PaymentRequestDto> paymentRequestDtoFlux);
}
