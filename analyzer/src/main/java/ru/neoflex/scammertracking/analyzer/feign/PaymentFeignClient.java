package ru.neoflex.scammertracking.analyzer.feign;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

//@FeignClient(value = "paymentFeign", url = "${paymentService.hostPort}/payment", configuration = DecoderConfiguration.class)
public interface PaymentFeignClient {

    @PostMapping("/last-payment")
    LastPaymentResponseDto getLastPaymentByPayerCardNumber(@RequestBody LastPaymentRequestDto payment);

    @PostMapping("/save")
    @ResponseStatus(value = HttpStatus.CREATED)
    String savePayment(@RequestBody PaymentRequestDto payment);
}
