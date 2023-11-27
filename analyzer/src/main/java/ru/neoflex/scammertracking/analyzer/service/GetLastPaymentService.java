package ru.neoflex.scammertracking.analyzer.service;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

import java.util.List;

public interface GetLastPaymentService {
    void process(List<PaymentRequestDto> paymentRequest);
}
