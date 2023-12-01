package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateLastPaymentDto {

    public AggregateLastPaymentDto(PaymentRequestDto lastPaymentRequestDto, LastPaymentResponseDto paymentResponse) {
        this.paymentRequest = lastPaymentRequestDto;
        this.paymentResponse = paymentResponse;
    }

    public AggregateLastPaymentDto() {
    }

    private PaymentRequestDto paymentRequest;
    private LastPaymentResponseDto paymentResponse;
}
