package ru.neoflex.scammertracking.paymentdb.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateLastPaymentDto {

    public AggregateLastPaymentDto(GetLastPaymentRequestDto paymentRequest, PaymentResponseDto paymentResponse) {
        this.paymentRequest = paymentRequest;
        this.paymentResponse = paymentResponse;
    }

    public AggregateLastPaymentDto() {
    }

    private GetLastPaymentRequestDto paymentRequest;
    private PaymentResponseDto paymentResponse;
}
