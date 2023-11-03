package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

import java.util.concurrent.atomic.AtomicBoolean;

public interface PaymentService {
    Mono<LastPaymentResponseDto> getLastPayment(PaymentRequestDto paymentRequest);
}
