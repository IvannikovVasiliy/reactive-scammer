package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentServiceImpl implements SavePaymentService {

    private final ClientService clientService;
    private final PaymentCacheService paymentCacheService;

    public Mono<Void> savePayment(Flux<SavePaymentRequestDto> savePaymentDtoFlux) {

        clientService
                .savePayment(savePaymentDtoFlux)
                .subscribe(new BaseSubscriber<SavePaymentResponseDto>() {

                    @Override
                    protected void hookOnNext(SavePaymentResponseDto value) {
                        super.hookOnNext(value);
//                        paymentCacheService.saveIfAbsent(value);
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        //super.hookOnError(throwable);
                        System.out.println();
                    }
                });

        return Mono.empty();
    }
}
