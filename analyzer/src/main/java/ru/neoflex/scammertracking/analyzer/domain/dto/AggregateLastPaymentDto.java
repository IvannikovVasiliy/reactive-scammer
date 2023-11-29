package ru.neoflex.scammertracking.analyzer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AggregateLastPaymentDto {

    public AggregateLastPaymentDto(LastPaymentRequestDto lastPaymentRequestDto, LastPaymentResponseDto paymentResponseDto) {
        this.lastPaymentRequestDto = lastPaymentRequestDto;
        this.paymentResponseDto = paymentResponseDto;
    }

    public AggregateLastPaymentDto() {
    }

    private LastPaymentRequestDto lastPaymentRequestDto;
    private LastPaymentResponseDto paymentResponseDto;
}
