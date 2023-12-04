package ru.neoflex.scammertracking.analyzer.resp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateLastPaymentDto {

    public AggregateLastPaymentDto(LastPaymentRequestDto paymentRequest, PaymentResponseDto paymentResponse) {
        this.paymentRequest = paymentRequest;
        this.paymentResponse = paymentResponse;
    }

    public AggregateLastPaymentDto() {
    }

    private LastPaymentRequestDto paymentRequest;
    private PaymentResponseDto paymentResponse;
}
