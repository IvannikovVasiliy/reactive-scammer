package ru.neoflex.scammertracking.analyzer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

import java.util.List;
import java.util.Map;

public interface ClientService {
    Flux<LastPaymentResponseDto> getLastPayment(List<PaymentRequestDto> payments);
    Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest);
}
