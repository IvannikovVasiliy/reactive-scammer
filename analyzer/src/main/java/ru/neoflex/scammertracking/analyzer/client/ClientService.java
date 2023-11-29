package ru.neoflex.scammertracking.analyzer.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;

import java.util.List;
import java.util.Map;

public interface ClientService {
    Flux<Map> getLastPayment(List<LastPaymentRequestDto> payments);
    Mono<Void> savePayment(SavePaymentRequestDto savePaymentRequest);
}
