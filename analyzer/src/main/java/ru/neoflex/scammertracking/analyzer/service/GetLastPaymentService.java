package ru.neoflex.scammertracking.analyzer.service;

import org.apache.kafka.common.network.Mode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.AnalyzeModel;

import java.util.List;

public interface GetLastPaymentService {
    Mono<Void> process(Flux<AggregateLastPaymentDto> paymentRequest);
}
