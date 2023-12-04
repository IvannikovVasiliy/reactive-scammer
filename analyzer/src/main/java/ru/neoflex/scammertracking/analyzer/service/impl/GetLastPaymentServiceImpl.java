package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.*;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetLastPaymentServiceImpl implements GetLastPaymentService {

    private final PaymentCacheRepository paymentCacheRepository;
    private final SourceMapperImplementation sourceMapper;
    private final ClientService clientService;
    private final GeoAnalyzer geoAnalyzer;
    private final SavePaymentService savePaymentService;
    private final PaymentProducer paymentProducer;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> process(Flux<AggregateLastPaymentDto> paymentRequests) {
        log.info("process. received paymentRequests");

        Flux<AggregateLastPaymentDto> fluxNonCache = paymentRequests.filter(x -> {
            if (x.getPaymentResponse() == null) {
                return true;
            }
            return false;
        });
        Flux<AggregateLastPaymentDto> fluxCache = paymentRequests.filter(x -> {
            if (x.getPaymentResponse() != null) {
                return true;
            }
            return false;
        });

        Flux<AggregateLastPaymentDto> f = clientService
                .getLastPayment(fluxNonCache);

        checkLastPaymentAsync(fluxCache);
        checkLastPaymentAsync(fluxNonCache);
        return Mono.empty();
    }


    private Mono<Void> checkLastPaymentAsync(Flux<AggregateLastPaymentDto> aggregatePaymentsFlux) {
        Flux<SavePaymentRequestDto> savePaymentFlux = aggregatePaymentsFlux
                .map(value -> {
                    boolean isTrusted;
                    if (value.getPaymentResponse() == null) {
                        isTrusted = true;
                    } else {
                        isTrusted = geoAnalyzer.checkPayment(value.getPaymentResponse(), value.getPaymentRequest());
                        if (isTrusted == false) {
                            throw new RuntimeException("Payment with id={} is suspicious, because it is failed in geo-analyzing-stage");
                        }
                    }

                    PaymentResponseDto paymentResponseDto = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(value.getPaymentRequest());
                    SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(value.getPaymentRequest());
                    SavePaymentDto savePaymentDto = new SavePaymentDto(isTrusted, savePaymentRequestDto, paymentResponseDto);
                    return savePaymentDto;
                })
                .onErrorResume(err ->
                        Mono.empty())
                .map(val -> val.getSavePaymentRequestDto());

        savePaymentService.savePayment(savePaymentFlux);


//        Flux<SavePaymentRequestDto> savePaymentFlux = aggregatePaymentsFlux
//                .flatMap(value -> {
//                    SavePaymentDto savePaymentDto = new SavePaymentDto();
//                    PaymentResponseDto paymentResponseDto = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(value.getPaymentRequest());
//                    savePaymentDto.setTrusted(true);
//                    savePaymentDto.setPaymentResponseDto(paymentResponseDto);
//                    SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(value.getPaymentRequest());
//                    savePaymentDto.setSavePaymentRequestDto(savePaymentRequestDto);
//                    return Mono.just(savePaymentDto);
//                })
//                .flatMap(x ->
//                        Mono.just(x.getSavePaymentRequestDto()));


//        WebClient
//                .create("http://localhost:8082/payment/save")
//                .post()
//                .body(savePaymentFlux, Flux.class)
//                .retrieve()
//                .bodyToFlux(Object.class)
//                .subscribe(new BaseSubscriber<Object>() {
//
//                    Subscription subscription;
//                    AtomicInteger ai = new AtomicInteger();
//
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
////                        super.hookOnSubscribe(subscription);
//                        this.subscription = subscription;
//                        subscription.request(100);
//                    }
//
//                    @Override
//                    protected void hookOnNext(Object value) {
//                        System.out.println();
////                        super.hookOnNext(value);
////                        if (ai.incrementAndGet() == 100) {
////                            ai.set(0);
////                            subscription.request(100);
////                        }
////                        System.out.println(value);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
////                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        //super.hookOnError(throwable);
//                    }
//                });

        return Mono.empty();
    }
//        Flux<AnalyzeModel> lastPaymentFlux = Flux.just();
//
//        Flux<AnalyzeModel> analyzeFlux = paymentRequests
//                .flatMap(paymentCache -> {
//                    LastPaymentResponseDto lastPaymentResponseDto = new LastPaymentResponseDto();
//                    AnalyzeModel analyzeModel = new AnalyzeModel();
//                    analyzeModel.setPaymentRequest(paymentCache);
//

//                    paymentCacheRepository
//                            .findPaymentByCardNumber(paymentCache.getPayerCardNumber())
//                            .subscribe(new BaseSubscriber<>() {
//
//                                Subscription subscription;
//                                boolean isPaymentMonoIsEmpty = true;
//
//                                @Override
//                                protected void hookOnSubscribe(Subscription subscription) {
//                                    super.hookOnSubscribe(subscription);
//                                    this.subscription = subscription;
//                                    subscription.request(1);
//                                }
//
//                                @Override
//                                protected void hookOnNext(PaymentEntity payment) {
//                                    super.hookOnNext(payment);
//                                    log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
//                                            payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
//                                    isPaymentMonoIsEmpty = false;
//                                    LastPaymentResponseDto lastPaymentResponse = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
//                                    analyzeModel.setLastPayment(lastPaymentResponse);
//                                    subscription.request(1);
//                                }
//                            });
//                    return Mono.just(analyzeModel);
//                });
//        getLastPaymentFromClientService(analyzeFlux);
//        return Mono.empty();

//        List<ConsumeMessage> payments = new ArrayList<>();
//        AtomicInteger counter = new AtomicInteger();
//        for (var paymentRequest : paymentRequests) {
//            PaymentResponseDto paymentResult =
//                    sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
//
//            paymentCacheRepository
//                    .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
//                    .subscribe(new BaseSubscriber<>() {
//
//                        Subscription subscription;
//                        boolean isPaymentMonoIsEmpty = true;
//
//                        @Override
//                        protected void hookOnSubscribe(Subscription subscription) {
//                            super.hookOnSubscribe(subscription);
//                            this.subscription = subscription;
//                            subscription.request(1);
//                        }
//
//                        @Override
//                        protected void hookOnNext(PaymentEntity payment) {
//                            super.hookOnNext(payment);
//                            log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
//                                    payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
//                            isPaymentMonoIsEmpty = false;
//                            subscription.request(1);
//                        }
//
//                        @Override
//                        protected void hookOnComplete() {
//                            super.hookOnComplete();
//                            log.info("hookOnComplete. Finish getting payment from cache.");
//                            counter.incrementAndGet();
//                            if (!isPaymentMonoIsEmpty) {
//                                payments.add(new ConsumeMessage(paymentRequest, paymentResult));
//                            } else {
//                                payments.add(new ConsumeMessage(paymentRequest, null));
//                            }
//
//                            if (counter.get() == paymentRequests.size()) {
//                                getLastPaymentFromClientService(payments);
//                            }
//                            log.info("3");
//                        }
//
//                        @Override
//                        protected void hookOnError(Throwable throwable) {
//                            log.error("hookOnError. error getting payment-cache, because of={}", throwable.getMessage());
//
////                            payments.add(Map.entry(paymentRequest, null));
//                            payments.add(new ConsumeMessage(paymentRequest, null));
//                            counter.incrementAndGet();
//
//                            if (counter.get() == paymentRequests.size()) {
//                                getLastPaymentFromClientService(payments);
//                            }
//                        }
//                    });
//        }

//    private void checkLastPaymentAsync(LastPaymentResponseDto lastPayment, PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
//        log.info("Input checkLastPaymentAsync. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
//                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentResult.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());
//
//        boolean isTrusted = geoAnalyzer.checkPayment(lastPayment, paymentRequest);
//        paymentResult.setTrusted(isTrusted);
//
//        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
//        savePaymentService.savePayment(isTrusted, savePaymentRequestDto, paymentResult);
//
//        log.info("Output checkLastPaymentAsync. Finish");
//    }

//    private Mono<Void> getLastPaymentFromClientService(Flux<AnalyzeModel> analyzeFlux) {
//        log.info("Input getLastPaymentFromClientService. received map with current payments and payments from cache");
//
//        List<AnalyzeModel> lastPayments = new ArrayList<>();
//        Flux<AnalyzeModel> lastPaymentFlux = Flux.fromIterable(lastPayments);
//        analyzeFlux.
//                doOnNext(val -> {
//                    if (val.getLastPayment() == null) {
//                        lastPayments.add(val);
//                    }
//                })
//                .subscribe();
//
//        Flux<AggregateLastPaymentDto> f = clientService
//                .getLastPayment(lastPaymentFlux);
//
//        checkLastPaymentAsync(f);
//        return Mono.empty();
//    }
//                .subscribe(new BaseSubscriber<AggregateLastPaymentDto>() {
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                    }
//
//                    @Override
//                    protected void hookOnNext(AggregateLastPaymentDto value) {
//                        super.hookOnNext(value);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        super.hookOnError(throwable);
//                    }
//                });
//                .subscribe(new BaseSubscriber<Map>() {

//            Subscription subscription;
//            AtomicInteger counter = new AtomicInteger();
//
//            @Override
//            protected void hookOnSubscribe(Subscription subscription) {
//                super.hookOnSubscribe(subscription);
//                log.info("hookOnSubscribe");
////                        this.subscription = subscription;
////                        subscription.request(1);
//            }
//
//            @Override
//            protected void hookOnNext(Map lastPayment) {
//                super.hookOnNext(lastPayment);
//                lastPayment.get(payments.get(0));
////                        subscription.request(1);
//                counter.incrementAndGet();
////                    checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
//            }
//
//            @Override
//            protected void hookOnComplete() {
//                super.hookOnComplete();
//            }
//
//            @Override
//            protected void hookOnError(Throwable throwable) {
////                        super.hookOnError(throwable);
//                log.error("getLastPaymentFromClientService hookOnError. error from ms-payment, because of {}", throwable.getMessage());
//
//                counter.incrementAndGet();
//                if (throwable instanceof WebClientResponseException.NotFound) {
//                    if (counter.get() == payments.size()) {
////                                SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
////                                savePaymentService.savePayment(true, savePaymentRequestDto, paymentResult);
//                    }
//                }
//            }
//        });
//
//
//        clientService
//                .getLastPayment(paymentsList)
//                .retryWhen(
//                        Retry
//                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
//                                .filter(throwable -> !(throwable instanceof NotFoundException))
//                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
////                                    sendBackoffMessageInKafka(paymentRequest);
//                                    throw new RuntimeException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
//                                }))
//                )
//                .subscribe(new BaseSubscriber<LastPaymentResponseDto>() {
//
//                    Subscription subscription;
//                    AtomicInteger counter = new AtomicInteger();
//
//                    List<>
//
//                    @Override
//
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        this.subscription = subscription;
//                        subscription.request(1);
//                    }
//
//                    @Override
//                    protected void hookOnNext(LastPaymentResponseDto lastPayment) {
//                        super.hookOnNext(lastPayment);
//                        subscription.request(1);
//                        log.info("hookOnNext. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } }",
//                                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate());
//                        counter.incrementAndGet();
////                    checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        super.hookOnError(throwable);
//                        log.error("getLastPaymentFromClientService hookOnError. error from ms-payment, because of {}", throwable.getMessage());
//
//                        counter.incrementAndGet();
//                        if (throwable instanceof WebClientResponseException.NotFound) {
//                            if (counter.get() == payments.size()) {
//                                SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
//                                savePaymentService.savePayment(true, savePaymentRequestDto, paymentResult);
//                            }
//                        }
//                    }
//                });
//    }


//    private void getLastPaymentFromClientService(List<ConsumeMessage> payments) {
//        log.info("Input getLastPaymentFromClientService. received map with current payments and payments from cache");
//
//        List<LastPaymentRequestDto> paymentsList = payments
//                .stream()
//                .map(payment -> new LastPaymentRequestDto(payment.getKey().getPayerCardNumber()))
//                .toList();
//
//        clientService
//                .getLastPayment(paymentsList)
//                .subscribe(new BaseSubscriber<AggregateLastPaymentDto>() {
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                    }
//
//                    @Override
//                    protected void hookOnNext(AggregateLastPaymentDto value) {
//                        super.hookOnNext(value);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        super.hookOnError(throwable);
//                    }
//                });
//                .subscribe(new BaseSubscriber<Map>() {
//
//                    Subscription subscription;
//                    AtomicInteger counter = new AtomicInteger();
//
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        log.info("hookOnSubscribe");
////                        this.subscription = subscription;
////                        subscription.request(1);
//                    }
//
//                    @Override
//                    protected void hookOnNext(Map lastPayment) {
//                        super.hookOnNext(lastPayment);
//                        lastPayment.get(payments.get(0));
////                        subscription.request(1);
//                        counter.incrementAndGet();
////                    checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
////                        super.hookOnError(throwable);
//                        log.error("getLastPaymentFromClientService hookOnError. error from ms-payment, because of {}", throwable.getMessage());
//
//                        counter.incrementAndGet();
//                        if (throwable instanceof WebClientResponseException.NotFound) {
//                            if (counter.get() == payments.size()) {
////                                SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
////                                savePaymentService.savePayment(true, savePaymentRequestDto, paymentResult);
//                            }
//                        }
//                    }
//                });


//        clientService
//                .getLastPayment(paymentsList)
//                .retryWhen(
//                        Retry
//                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
//                                .filter(throwable -> !(throwable instanceof NotFoundException))
//                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
////                                    sendBackoffMessageInKafka(paymentRequest);
//                                    throw new RuntimeException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
//                                }))
//                )
//                .subscribe(new BaseSubscriber<LastPaymentResponseDto>() {
//
//                    Subscription subscription;
//                    AtomicInteger counter = new AtomicInteger();
//
//                    List<>
//
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        this.subscription = subscription;
//                        subscription.request(1);
//                    }
//
//                    @Override
//                    protected void hookOnNext(LastPaymentResponseDto lastPayment) {
//                        super.hookOnNext(lastPayment);
//                        subscription.request(1);
//                        log.info("hookOnNext. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } }",
//                                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate());
//                        counter.incrementAndGet();
////                    checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        super.hookOnError(throwable);
//                        log.error("getLastPaymentFromClientService hookOnError. error from ms-payment, because of {}", throwable.getMessage());
//
//                        counter.incrementAndGet();
//                        if (throwable instanceof WebClientResponseException.NotFound) {
//                            if (counter.get() == payments.size()) {
//                                SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
//                                savePaymentService.savePayment(true, savePaymentRequestDto, paymentResult);
//                            }
//                        }
//                    }
//                });
//    }

    private void sendBackoffMessageInKafka(PaymentRequestDto paymentRequest) {
        try {
            byte[] backoffBytesPayment = objectMapper.writeValueAsBytes(paymentRequest);
            paymentProducer.sendBackoffMessage(String.valueOf(paymentRequest.getId()), backoffBytesPayment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
