package ru.neoflex.scammertracking.paymentdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.GetLastPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

import java.time.Duration;

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
}
