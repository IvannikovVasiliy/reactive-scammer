package ru.neoflex.scammertracking.paymentdb.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.EditPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.GetLastPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;

import java.util.List;

public interface PaymentService {
    Mono<Void> savePayment(@Valid SavePaymentRequestDto payment);
//    Mono<PaymentResponseDto> getLastPayment(@Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60") String cardNumber);
    Flux<PaymentResponseDto> getLastPayment(@Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60") List<GetLastPaymentRequestDto> payments);
    Mono<Void> putPayment(@Valid EditPaymentRequestDto editPaymentRequestDto);
    Mono<Void> deletePaymentById(@NotNull Long id);
//    PaymentResponseDto getLastPayment(Long id);
//    void insertNullPayments(String idCardNumber);
//    void insertPaymentBuffer(String idCardNumber);
//    UpdatePaymentResponseDto updatePayments(UpdatePaymentRequestDto updatePaymentRequest, long id);
}