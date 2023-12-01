package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.*;

@Getter
@Setter
public class SavePaymentDto {

    public SavePaymentDto(boolean isTrusted, SavePaymentRequestDto savePaymentRequestDto, PaymentResponseDto paymentResponseDto) {
        this.isTrusted = isTrusted;
        this.savePaymentRequestDto = savePaymentRequestDto;
        this.paymentResponseDto = paymentResponseDto;
    }

    public SavePaymentDto() { }

    private boolean isTrusted;
    private SavePaymentRequestDto savePaymentRequestDto;
    private PaymentResponseDto paymentResponseDto;
}
