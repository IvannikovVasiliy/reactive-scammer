package ru.neoflex.scammertracking.analyzer.service;

import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface PaymentCacheService {

    void saveIfAbsent(SavePaymentRequestDto savePaymentRequest);
}
