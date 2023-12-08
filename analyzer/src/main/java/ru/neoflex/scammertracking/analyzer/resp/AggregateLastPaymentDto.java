package ru.neoflex.scammertracking.analyzer.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AggregateLastPaymentDto {

    private LastPaymentRequestDto paymentRequest;
    private PaymentResponseDto paymentResponse;
}
