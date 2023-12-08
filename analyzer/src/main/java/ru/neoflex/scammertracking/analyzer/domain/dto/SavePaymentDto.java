package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.*;

@Getter
@Setter
public class SavePaymentDto {

    public SavePaymentDto(SavePaymentRequestDto savePaymentRequestDto, PaymentResponseDto paymentResponseDto) {
        this.savePaymentRequestDto = savePaymentRequestDto;
        this.paymentResponseDto = paymentResponseDto;
    }

    public SavePaymentDto() { }

    private SavePaymentRequestDto savePaymentRequestDto;
    private PaymentResponseDto paymentResponseDto;
}
