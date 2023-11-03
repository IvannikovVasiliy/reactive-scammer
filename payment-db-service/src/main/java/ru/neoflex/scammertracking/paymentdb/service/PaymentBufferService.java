package ru.neoflex.scammertracking.paymentdb.service;

import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentBufferEntity;

public interface PaymentBufferService {
    PaymentBufferEntity getPaymentByCardNumber(String cardNumber);
    void insertPaymentBuffer(String idCardNumber);
}
