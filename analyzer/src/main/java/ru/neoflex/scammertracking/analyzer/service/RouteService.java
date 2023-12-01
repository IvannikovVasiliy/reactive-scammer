package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;

public interface RouteService {

    Mono<Void> router(Flux<PaymentRequestDto> flux);
}
