package ru.neoflex.scammertracking.analyzer.router;

import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;

public interface GetLastPaymentRouter {
    void process(Flux<AggregateGetLastPaymentDto> paymentRequest);
}
