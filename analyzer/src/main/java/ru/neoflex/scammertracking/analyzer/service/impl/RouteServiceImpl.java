package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.PreAnalyzerPayment;
import ru.neoflex.scammertracking.analyzer.service.RouteService;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final PreAnalyzerPayment preAnalyzerPayment;
    private final GetLastPaymentService lastPaymentService;

    @Override
    public Mono<Void> router(Flux<PaymentRequestDto> flux) {

//        Flux<PaymentRequestDto> paymentsFlux = flux.flatMap(val -> {
//            PaymentRequestDto paymentRequestDtoMono = preAnalyzerPayment.preAnalyzeConsumeMessage(val);
//            return Mono.just(paymentRequestDtoMono);
//        });
//        lastPaymentService.process(paymentsFlux).subscribe();


        return Mono.empty();
    }
}
