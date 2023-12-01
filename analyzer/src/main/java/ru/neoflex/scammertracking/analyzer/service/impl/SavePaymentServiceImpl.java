package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.service.PaymentCacheService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavePaymentServiceImpl implements SavePaymentService {

    private final ClientService clientService;
    private final PaymentProducer paymentProducer;
    private final PaymentCacheService paymentCacheService;
    private final ObjectMapper objectMapper;

    public Mono<Void> savePayment(Flux<SavePaymentDto> savePaymentDtoFlux) {
//        savePaymentDtoFlux.subscribe(new BaseSubscriber<SavePaymentDto>() {
//            @Override
//            protected void hookOnNext(SavePaymentDto value) {
//                super.hookOnNext(value);
//            }
//
//            @Override
//            protected void hookOnError(Throwable throwable) {
//                super.hookOnError(throwable);
//            }
//        });

        savePaymentDtoFlux
                .flatMap(val ->
                        Mono.just(val.getSavePaymentRequestDto()));

        WebClient
                .create("http://localhost:8082/payment/save")
                .post()
                .body(savePaymentDtoFlux, Flux.class)
                .retrieve()
                .bodyToFlux(Object.class)
                .subscribe(new BaseSubscriber<Object>() {

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                    }

                    @Override
                    protected void hookOnNext(Object value) {
                        super.hookOnNext(value);
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        super.hookOnError(throwable);
                    }
                });

        return Mono.empty();
    }
//
//        paymentResult.setTrusted(isTrusted);
//        if (isTrusted) {
//            clientService
//                    .savePayment(savePaymentRequest)
//                    .doOnSuccess(payment -> {
//                        log.info("Complete request to save payment to ms-payment");
//                        paymentCacheService.saveIfAbsent(savePaymentRequest);
//                        paymentProducer.sendCheckedMessage(paymentResult);
//                    })
//                    .doOnError(throwable -> {
//                        log.error("savePayment error. error from ms-payment, because of {}", throwable.getMessage());
//
//                        if (throwable instanceof BadRequestException) {
//                            byte[] paymentResultBytes;
//                            try {
//                                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
//                            } catch (JsonProcessingException e) {
//                                log.error("Unable to parse paymentResult into bytes");
//                                throw new RuntimeException(throwable.getMessage());
//                            }
//                            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
//                            log.info("Response. Sent message in suspicious-topic, BadRequest because of {}", throwable.getMessage());
//                        }
//                    })
//                    .retryWhen(
//                            Retry
//                                    .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
//                                    .filter(throwable -> !(throwable instanceof BadRequestException))
//                                    .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
//                                        throw new RuntimeException("Error savePayment. External ms-payment failed to process after max retries");
//                                    }))
//                    )
//                    .subscribe(new BaseSubscriber<Void>() {
//
//                        Subscription subscription;
//
//                        @Override
//                        protected void hookOnSubscribe(Subscription subscription) {
//                            super.hookOnSubscribe(subscription);
//                            this.subscription = subscription;
//                            subscription.request(1);
//                        }
//
//                        @Override
//                        protected void hookOnNext(Void value) {
//                            super.hookOnNext(value);
//                            subscription.request(1);
//                        }
//                    });
//        } else {
//            byte[] paymentResultBytes;
//            try {
//                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
//            } catch (JsonProcessingException e) {
//                log.error("Unable to parse paymentResult into bytes");
//                throw new RuntimeException(e);
//            }
//            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
//            log.info("Response. Sent message in suspicious-topic, because latitude={}, longitude={}",
//                    savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude());
//        }

//    public void savePayment(boolean isTrusted, SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult) {
//        log.info("Received. isTrusted={}. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n Payment result={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {}}", isTrusted, savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());
//
//        paymentResult.setTrusted(isTrusted);
//        if (isTrusted) {
//            clientService
//                    .savePayment(savePaymentRequest)
//                    .doOnSuccess(payment -> {
//                        log.info("Complete request to save payment to ms-payment");
//                        paymentCacheService.saveIfAbsent(savePaymentRequest);
//                        paymentProducer.sendCheckedMessage(paymentResult);
//                    })
//                    .doOnError(throwable -> {
//                        log.error("savePayment error. error from ms-payment, because of {}", throwable.getMessage());
//
//                        if (throwable instanceof BadRequestException) {
//                            byte[] paymentResultBytes;
//                            try {
//                                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
//                            } catch (JsonProcessingException e) {
//                                log.error("Unable to parse paymentResult into bytes");
//                                throw new RuntimeException(throwable.getMessage());
//                            }
//                            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
//                            log.info("Response. Sent message in suspicious-topic, BadRequest because of {}", throwable.getMessage());
//                        }
//                    })
//                    .retryWhen(
//                            Retry
//                                    .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
//                                    .filter(throwable -> !(throwable instanceof BadRequestException))
//                                    .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
//                                        throw new RuntimeException("Error savePayment. External ms-payment failed to process after max retries");
//                                    }))
//                    )
//                    .subscribe(new BaseSubscriber<Void>() {
//
//                        Subscription subscription;
//
//                        @Override
//                        protected void hookOnSubscribe(Subscription subscription) {
//                            super.hookOnSubscribe(subscription);
//                            this.subscription = subscription;
//                            subscription.request(1);
//                        }
//
//                        @Override
//                        protected void hookOnNext(Void value) {
//                            super.hookOnNext(value);
//                            subscription.request(1);
//                        }
//                    });
//        } else {
//            byte[] paymentResultBytes;
//            try {
//                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
//            } catch (JsonProcessingException e) {
//                log.error("Unable to parse paymentResult into bytes");
//                throw new RuntimeException(e);
//            }
//            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
//            log.info("Response. Sent message in suspicious-topic, because latitude={}, longitude={}",
//                    savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude());
//        }
//    }
}
