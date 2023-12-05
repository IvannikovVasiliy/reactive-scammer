package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;

public interface PaymentCacheService {
    void saveIfAbsent(SavePaymentResponseDto savePaymentRequest);
}
