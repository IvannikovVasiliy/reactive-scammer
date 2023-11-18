package ru.neoflex.scammertracking.analyzer.service;

import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface GetLastPaymentService {
    void process(PaymentRequestDto paymentRequest);
}
