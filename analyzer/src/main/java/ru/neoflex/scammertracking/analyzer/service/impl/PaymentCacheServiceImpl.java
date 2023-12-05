package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCacheServiceImpl implements PaymentCacheService {

    private final PaymentCacheRepository paymentCacheRepository;
    private final SourceMapperImplementation sourceMapper;

    @Override
    public void saveIfAbsent(SavePaymentResponseDto savePaymentRequest) {
        log.debug("saveIfAbsent. savePaymentRequest={}", savePaymentRequest);

        paymentCacheRepository
                .findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber())
                .subscribe(new BaseSubscriber<>() {

                    Subscription subscription;
                    PaymentEntity payment = null;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                        log.info("hookOnSubscribe. Subscribe to payment-service for saving payment");
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    protected void hookOnNext(PaymentEntity payment) {
                        super.hookOnNext(payment);
                        log.info("hookOnNext. get payment from cache. payment={ payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
                        this.payment = payment;
                        subscription.request(1);
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        log.info("hookOnComplete. Complete getting cache payment");
                        if (payment == null) {
                            PaymentEntity paymentEntity = sourceMapper.sourceFromSavePaymentResponseDtoToPaymentEntity(savePaymentRequest);
                            paymentCacheRepository
                                    .save(paymentEntity)
                                    .doOnError(err -> {
                                        System.out.println(err);
                                    })
                                    .doOnSuccess(payment -> paymentCacheRepository
                                            .expire()
                                            .doOnError(error -> log.error("Error. Expiration date not setted to payment cache"))
                                            .subscribe(new BaseSubscriber<>() {

                                                Subscription subscription;

                                                @Override
                                                protected void hookOnSubscribe(Subscription subscription) {
                                                    super.hookOnSubscribe(subscription);
                                                    this.subscription = subscription;
                                                    subscription.request(1);
                                                }

                                                @Override
                                                protected void hookOnNext(Boolean value) {
                                                    super.hookOnNext(value);
                                                    subscription.request(1);
                                                }
                                            }))
                                    .subscribe(new BaseSubscriber<Boolean>() {

                                        Subscription subscription;

                                        @Override
                                        protected void hookOnSubscribe(Subscription subscription) {
                                            super.hookOnSubscribe(subscription);
                                            this.subscription = subscription;
                                            subscription.request(1);
                                        }

                                        @Override
                                        protected void hookOnNext(Boolean value) {
                                            super.hookOnNext(value);
                                            subscription.request(1);
                                        }
                                    });
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        log.error("hookOnError. Error saving payment in redis, because of={}", throwable.getMessage());
                    }
                });
    }
}
