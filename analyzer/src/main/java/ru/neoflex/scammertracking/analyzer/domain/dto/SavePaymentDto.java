package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.Data;

@Data
public class SavePaymentDto {
    private boolean isTrusted;
    private SavePaymentRequestDto savePaymentRequestDto;
    private PaymentResponseDto paymentResponseDto;
}
