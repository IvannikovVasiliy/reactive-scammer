package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.error.exception.NotFoundException;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.router.RouterPayment;
import ru.neoflex.scammertracking.analyzer.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

//    @Value("${hostPort.paymentService}")
//    private String paymentServiceHostPort;

    private final PaymentCacheRepository paymentCacheRepository;
    //    private final FeignService feignService;
    private final SourceMapperImplementation sourceMapper;
    private final ClientService clientService;
    private final GeoAnalyzer geoAnalyzer;
    private final RouterPayment routerPayment;

    @Override
    public void processLastPayment(PaymentRequestDto paymentRequest) {
        PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
        paymentCacheRepository
                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                .subscribe(new BaseSubscriber<>() {

                    PaymentEntity payment = null;
                    boolean isPaymentMonoIsEmpty = true;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                        log.info("hookOnSubscribe. Subscribe on mono with payment in cache");
                    }

                    @Override
                    protected void hookOnNext(PaymentEntity payment) {
                        super.hookOnNext(payment);
                        log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPaymen={}, latitude={}, longitude={}, datePayment={}, dateUpdating={} }",
                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment(), payment.getDateUpdating());
                        this.payment = payment;
                        isPaymentMonoIsEmpty = false;
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        log.info("hookOnComplete. Finish getting payment from cache.");
                        if (isPaymentMonoIsEmpty) {
                            clientService
                                    .getLastPayment(paymentRequest)
                                    .subscribe(new BaseSubscriber<>() {
                                        LastPaymentResponseDto lastPayment = null;

                                        @Override
                                        protected void hookOnSubscribe(Subscription subscription) {
                                            super.hookOnSubscribe(subscription);
                                            log.info("hookOnSubscribe. Subscribe to payment-service for getting last payment");
                                        }

                                        @Override
                                        protected void hookOnNext(LastPaymentResponseDto lastPayment) {
                                            super.hookOnNext(lastPayment);
                                            log.info("hookOnNext. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } }",
                                                    lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate());
                                            checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
                                        }

                                        @Override
                                        protected void hookOnError(Throwable throwable) {
                                            log.error("hookOnError. Error saving payment in redis, because of={}", throwable.getMessage());

                                            if (throwable instanceof NotFoundException) {
                                                SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
                                                routerPayment.routePayment(true, savePaymentRequestDto, paymentResult);
                                            } else {
                                                super.hookOnError(throwable);
                                            }
                                        }
                                    });
                        } else {
                            LastPaymentResponseDto lastPayment = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                            checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        log.error("hookOnError. error getting payment-cache, because of={}", throwable.getMessage());
                        super.hookOnError(throwable);
                    }
                });

//        log.info("Input getLastPayment. paymentRequest={ } paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
//                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());
//
//        PaymentEntity paymentEntity = new PaymentEntity();
//        paymentCacheRepository
//                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
////                .flatMap(paymentCacheEntity -> {
////                    Mono<LastPaymentResponseDto> lastPaymentResponse = null;
////                    if (null != paymentCacheEntity) {
////                        lastPaymentResponse = Mono.just(
////                                sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(paymentCacheEntity)
////                        );
////                        log.info("Response cache. Last payment response from payment-ms.");
////                    } else {
////                        lastPaymentResponse = clientService.getLastPayment(paymentRequest);
////                        log.info("Response. Cache does not exist. WebClient return last payment response.");
////                    }
////
////                    return lastPaymentResponse;
////                })
//                .doOnError(s ->
//                        System.out.println(s))
//                .doOnNext(p ->
//                        System.out.println(p))
//                .subscribe(new BaseSubscriber<PaymentEntity>() {
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        System.out.println("start");
//                    }
//
//                    @Override
//                    protected void hookOnNext(PaymentEntity value) {
//                        super.hookOnNext(value);
//                        System.out.println("next");
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                        System.out.println("finish");
//                    }
//                });

//        return paymentCacheRepository
//                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
//                .flatMap(paymentCacheEntity -> {
//                    Mono<LastPaymentResponseDto> lastPaymentResponse = null;
//                    if (null != paymentCacheEntity) {
//                        lastPaymentResponse = Mono.just(
//                                sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(paymentCacheEntity)
//                        );
//                        log.info("Response cache. Last payment response from payment-ms.");
//                    } else {
//                        lastPaymentResponse = clientService.getLastPayment(paymentRequest);
//                        log.info("Response. Cache does not exist. WebClient return last payment response.");
//                    }
//
//                    return lastPaymentResponse;
//                });
    }

    @Override
    public void savePaymentAsync(SavePaymentRequestDto savePaymentRequest) {
        log.info("savePaymentAsync. savePaymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate());

//        boolean isCacheEmpty = paymentCacheRepository.findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber()) == null;
//        if (isCacheEmpty) {
//            PaymentEntity paymentEntity = sourceMapper.sourceFromSavePaymentRequestDtoToPaymentEntity(savePaymentRequest);
//            paymentEntity.setDateUpdating(LocalDateTime.now());
//            paymentCacheRepository.save(paymentEntity);
//
//            log.info("savePaymentAsync. Save cache in redis");
//        }

        paymentCacheRepository
                .findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber())
                .subscribe(new BaseSubscriber<>() {

                    boolean isCacheEmpty = true;
                    PaymentEntity payment = null;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                        log.info("hookOnSubscribe. Subscribe to payment-service for saving payment");
                    }

                    @Override
                    protected void hookOnNext(PaymentEntity payment) {
                        super.hookOnNext(payment);
                        log.info("hookOnNext. get payment from cache. payment={ payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={}, dateUpdating={} }",
                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment(), payment.getDateUpdating());
                        this.payment = payment;
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        log.info("hookOnComplete. Complete getting cache payment");
                        if (payment == null) {
                            PaymentEntity paymentEntity = sourceMapper.sourceFromSavePaymentRequestDtoToPaymentEntity(savePaymentRequest);
                            paymentCacheRepository
                                    .save(paymentEntity)
                                    .subscribe();
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        log.error("hookOnError. Error saving payment in redis, because of={}", throwable.getMessage());
                    }
                });
    }

    private void checkLastPaymentAsync(LastPaymentResponseDto lastPayment, PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
        log.info("Input getLastPaymentAsync. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        boolean isTrusted = geoAnalyzer.checkPayment(lastPayment, paymentRequest);
        paymentResult.setTrusted(isTrusted);

        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
        routerPayment.routePayment(isTrusted, savePaymentRequestDto, paymentResult);

        log.info("Output getLastPaymentAsync. Finish");
    }
}
