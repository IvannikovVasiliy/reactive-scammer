package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateGetLastPaymentDto {

    public AggregateGetLastPaymentDto(PaymentRequestDto lastPaymentRequestDto, LastPaymentResponseDto paymentResponse) {
        this.paymentRequest = lastPaymentRequestDto;
        this.paymentResponse = paymentResponse;
    }

    public AggregateGetLastPaymentDto() {
    }

    private PaymentRequestDto paymentRequest;
    private LastPaymentResponseDto paymentResponse;
}
