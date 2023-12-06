package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;

public interface PaymentCacheService {
    Mono<Void> saveIfAbsent(SavePaymentResponseDto savePaymentRequest);
}
