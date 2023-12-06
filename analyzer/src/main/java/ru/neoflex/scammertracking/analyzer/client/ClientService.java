package ru.neoflex.scammertracking.analyzer.client;

import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;

public interface ClientService {
    Flux<AggregateGetLastPaymentDto> getLastPayment(Flux<AggregateGetLastPaymentDto> payments);

    //    Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest);
    Flux<SavePaymentResponseDto> savePayment(Flux<SavePaymentRequestDto> savePaymentRequest);
}
