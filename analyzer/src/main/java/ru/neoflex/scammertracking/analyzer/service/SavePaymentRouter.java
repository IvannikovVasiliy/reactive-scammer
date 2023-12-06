package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface SavePaymentRouter {
    //    void savePayment(boolean isTrusted, SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult);
    Mono<Void> savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux);
}
