package ru.neoflex.scammertracking.analyzer.domain.model;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public class ConsumeMessage {

    public ConsumeMessage(String key, PaymentRequestDto paymentRequest) {
        this.key = key;
        this.paymentRequest = paymentRequest;
    }

    private String key;
    private PaymentRequestDto paymentRequest;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public PaymentRequestDto getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentRequestDto paymentRequest) {
        this.paymentRequest = paymentRequest;
    }
}
