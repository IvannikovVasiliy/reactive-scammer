package ru.neoflex.scammertracking.analyzer.service;

import org.apache.kafka.common.network.Mode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;

public interface SavePaymentService {
    //    void savePayment(boolean isTrusted, SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult);
    Mono<Void> savePayment(Flux<SavePaymentDto> savePaymentDtoFlux);
}
