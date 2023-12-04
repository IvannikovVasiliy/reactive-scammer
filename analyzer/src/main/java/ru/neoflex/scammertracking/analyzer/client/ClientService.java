package ru.neoflex.scammertracking.analyzer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.model.AnalyzeModel;

public interface ClientService {
    Flux<AggregateLastPaymentDto> getLastPayment(Flux<AggregateLastPaymentDto> payments);

    //    Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest);
    Flux<SavePaymentResponseDto> savePayment(Flux<SavePaymentRequestDto> savePaymentRequest);
}
