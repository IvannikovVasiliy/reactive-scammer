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
    public Mono<String> savePayment(@Valid @RequestBody SavePaymentRequestDto payment) {
        paymentService.savePayment(payment);

        return Mono.just("The payment was saved");
    }

    //    @Autowired
//    public PaymentController(CommonPaymentService commonPaymentService) {
//        this.commonPaymentService = commonPaymentService;
//    }
//
//    private CommonPaymentService commonPaymentService;

//    @PostMapping("/last-payment")
//    public PaymentResponseDto getLastPaymentByReceiverCardNumber(@RequestBody GetLastPaymentRequestDto payment) {
//        PaymentResponseDto responseDto = commonPaymentService.getLastPayment(payment.getCardNumber());
//
//        return responseDto;
//    }
//
//    @PostMapping
//    @ResponseStatus(value = HttpStatus.CREATED)
//    public String createPayment(@RequestBody CreatePaymentRequestDto paymentRequest) {
//        commonPaymentService.insertPayments(paymentRequest.getIdCardNumber());
//        String response = "The rows were created";
//
//        return response;
//    }
//
//    @PutMapping
//    public UpdatePaymentResponseDto updatePayment(@RequestBody UpdatePaymentRequestDto updatePaymentRequest) {
//        UpdatePaymentResponseDto updateResponse = commonPaymentService.updatePayments(updatePaymentRequest);
//
//        return updateResponse;
//    }
}
