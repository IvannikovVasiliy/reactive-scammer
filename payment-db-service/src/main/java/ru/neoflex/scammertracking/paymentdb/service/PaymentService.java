package ru.neoflex.scammertracking.paymentdb.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentService {
    Flux<String> savePayment(/*@Valid*/ Flux<SavePaymentRequestDto> payment);

    //    Mono<PaymentResponseDto> getLastPayment(@Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60") String cardNumber);
    Flux<AggregateLastPaymentDto> getLastPayment(/*@Size(min = 6, max = 60, message = "The length of cardNumber should be between 6 and 60")*/ Flux<AggregateLastPaymentDto> payments);

    Mono<Void> putPayment(@Valid EditPaymentRequestDto editPaymentRequestDto);

    Mono<Void> deletePaymentById(@NotNull Long id);
//    PaymentResponseDto getLastPayment(Long id);
//    void insertNullPayments(String idCardNumber);
//    void insertPaymentBuffer(String idCardNumber);
//    UpdatePaymentResponseDto updatePayments(UpdatePaymentRequestDto updatePaymentRequest, long id);
}