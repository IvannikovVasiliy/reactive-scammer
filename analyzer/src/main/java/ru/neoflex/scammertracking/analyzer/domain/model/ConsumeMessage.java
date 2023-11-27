package ru.neoflex.scammertracking.analyzer.domain.model;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;

public class ConsumeMessage {

    public ConsumeMessage(PaymentRequestDto key, PaymentResponseDto paymentRequest) {
        this.key = key;
        this.paymentRequest = paymentRequest;
    }

    private PaymentRequestDto key;
    private PaymentResponseDto paymentRequest;

    public PaymentRequestDto getKey() {
        return key;
    }

    public void setKey(PaymentRequestDto key) {
        this.key = key;
    }

    public PaymentResponseDto getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentResponseDto paymentRequest) {
        this.paymentRequest = paymentRequest;
    }
}
