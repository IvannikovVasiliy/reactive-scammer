package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;

public interface GetLastPaymentService {
    Mono<Void> process(Flux<AggregateGetLastPaymentDto> paymentRequest);
}
