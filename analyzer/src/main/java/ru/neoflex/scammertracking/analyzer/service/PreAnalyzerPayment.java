package ru.neoflex.scammertracking.analyzer.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.model.ConsumeMessage;

import java.util.List;
import java.util.Map;

public interface PreAnalyzerPayment {
    Mono<Void> preAnalyzeConsumeMessage(Flux<PaymentRequestDto> paymentRequestDtos);
}
