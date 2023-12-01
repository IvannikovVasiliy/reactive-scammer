package ru.neoflex.scammertracking.paymentdb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.*;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

//    @PostMapping("/last-payment")
//    public Mono<PaymentResponseDto> getLastPaymentByPayerCardNumber(@Valid @RequestBody GetLastPaymentRequestDto payment) {
//        Mono<PaymentResponseDto> responseDto = paymentService.getLastPayment(payment.getCardNumber());
//
//        return responseDto;
//    }

    @PostMapping(value = "/last-payment")
    public Flux<AggregateLastPaymentDto> getLastPaymentByPayerCardNumber(
            /*@Valid*/ @RequestBody Flux<AggregateLastPaymentDto> payments
    ) {
        log.info("Request last-payment");
        Flux<AggregateLastPaymentDto> responseDto = paymentService.getLastPayment(payments);

        return responseDto.flatMap(x ->
                Mono.just(x));
    }

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Flux<Object> savePayment(/*@Valid*/ @RequestBody Flux<SavePaymentRequestDto> payment) {
//        payment.subscribe(new BaseSubscriber<SavePaymentRequestDto>() {
//            @Override
//            protected void hookOnSubscribe(Subscription subscription) {
//                super.hookOnSubscribe(subscription);
//            }
//
//            @Override
//            protected void hookOnNext(SavePaymentRequestDto value) {
//                super.hookOnNext(value);
//            }
//
//            @Override
//            protected void hookOnComplete() {
//                super.hookOnComplete();
//            }
//
//            @Override
//            protected void hookOnError(Throwable throwable) {
//                super.hookOnError(throwable);
//            }
//        });

        Flux<String> saveResponse = paymentService.savePayment(payment);

        return saveResponse.flatMap(val -> {
            return Mono.just(val);
        });
//        return Flux.just("str");
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
