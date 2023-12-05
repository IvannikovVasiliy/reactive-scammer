package ru.neoflex.scammertracking.paymentdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.*;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/last-payment")
    public Mono<PaymentResponseDto> getLastPaymentByPayerCardNumber(@Valid @RequestBody GetLastPaymentRequestDto payment) {
        Mono<PaymentResponseDto> responseDto = paymentService.getLastPayment(payment.getCardNumber());

        return responseDto;
    }

    @PostMapping("/save")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Mono<Void> savePayment(@Valid @RequestBody SavePaymentRequestDto payment) {
        Mono<Void> saveResponse = paymentService.savePayment(payment);

        return saveResponse;
    }

    @PutMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public Mono<Void> putPayment(@Valid @RequestBody EditPaymentRequestDto editPaymentRequestDto) {
        Mono<Void> putPaymentResponse = paymentService.putPayment(editPaymentRequestDto);

        return putPaymentResponse;
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public Mono<Void> deletePayment(@Valid @RequestBody DeletePaymentRequestDto deletePaymentRequestDto) {
        return paymentService.deletePaymentById(deletePaymentRequestDto.getId());
    }
}
