package ru.neoflex.scammertracking.analyzer.router.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import ru.neoflex.scammertracking.analyzer.check.PaymentChecker;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.AggregateGetLastPaymentDto;
import ru.neoflex.scammertracking.analyzer.router.GetLastPaymentRouter;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLastPaymentRouterImpl implements GetLastPaymentRouter {

    private final ClientService clientService;
    private final PaymentChecker paymentChecker;

    @Override
    public void process(Flux<AggregateGetLastPaymentDto> paymentRequestsFlux) {
        List<AggregateGetLastPaymentDto> listCache = new ArrayList<>();
        List<AggregateGetLastPaymentDto> listNonCache = new ArrayList<>();

        Flux<AggregateGetLastPaymentDto> fluxNonCache = Flux.fromIterable(listNonCache);
        Flux<AggregateGetLastPaymentDto> fluxCache = Flux.fromIterable(listCache);
        paymentRequestsFlux
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnNext(AggregateGetLastPaymentDto val) {
                        super.hookOnNext(val);
                        if (val.getPaymentResponse() == null) {
                            listNonCache.add(val);
                        } else {
                            listCache.add(val);
                        }
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        Flux<AggregateGetLastPaymentDto> lastPaymentFlux = clientService
                                .getLastPayment(fluxNonCache);

                        paymentChecker.checkLastPayment(lastPaymentFlux);
                        paymentChecker.checkLastPayment(fluxCache);
                    }
                });
    }
}
