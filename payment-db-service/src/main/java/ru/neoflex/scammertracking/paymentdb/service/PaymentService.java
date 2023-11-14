package ru.neoflex.scammertracking.paymentdb.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.EditPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;

public interface PaymentService {
    Mono<Void> savePayment(@Valid SavePaymentRequestDto payment);
    Mono<PaymentResponseDto> getLastPayment(@Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60") String cardNumber);
    Mono<Void> putPayment(@Valid EditPaymentRequestDto editPaymentRequestDto);
//    PaymentResponseDto getLastPayment(Long id);
//    void insertNullPayments(String idCardNumber);
//    void insertPaymentBuffer(String idCardNumber);
//    UpdatePaymentResponseDto updatePayments(UpdatePaymentRequestDto updatePaymentRequest, long id);
}