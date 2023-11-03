package ru.neoflex.scammertracking.analyzer.client;

import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface ClientService {
    Mono<LastPaymentResponseDto> getLastPayment(PaymentRequestDto paymentRequest);
    Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest);
}
