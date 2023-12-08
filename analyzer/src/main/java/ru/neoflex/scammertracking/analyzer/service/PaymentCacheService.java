package ru.neoflex.scammertracking.analyzer.service;

import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;

public interface PaymentCacheService {
    void saveIfAbsent(SavePaymentResponseDto savePaymentRequest);
}
