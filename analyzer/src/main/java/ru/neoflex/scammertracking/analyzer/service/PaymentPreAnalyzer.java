package ru.neoflex.scammertracking.analyzer.service;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface PaymentPreAnalyzer {
    void preAnalyzeConsumeMessage(String key, PaymentRequestDto paymentRequest) throws Exception;
}
