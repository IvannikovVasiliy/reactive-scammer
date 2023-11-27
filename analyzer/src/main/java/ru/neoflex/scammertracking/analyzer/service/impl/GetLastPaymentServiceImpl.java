package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.exception.NotFoundException;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.service.GetLastPaymentService;
import ru.neoflex.scammertracking.analyzer.service.SavePaymentService;
import ru.neoflex.scammertracking.analyzer.util.Constants;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    public void process(List<PaymentRequestDto> paymentRequests) {
        log.info("process. received paymentRequests");

        List<Map.Entry<PaymentRequestDto, PaymentResponseDto>> payments = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        for (var paymentRequest : paymentRequests) {
            PaymentResponseDto paymentResult =
                    sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);

            paymentCacheRepository
                    .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                    .subscribe(new BaseSubscriber<>() {

                        Subscription subscription;
                        boolean isPaymentMonoIsEmpty = true;

                        @Override
                        protected void hookOnSubscribe(Subscription subscription) {
                            super.hookOnSubscribe(subscription);
//                        this.subscription = subscription;
//                        subscription.request(1);
                        }

                        @Override
                        protected void hookOnNext(PaymentEntity payment) {
                            super.hookOnNext(payment);
                            log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
                                    payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
                            isPaymentMonoIsEmpty = false;
//                        subscription.request(1);
                        }

                        @Override
                        protected void hookOnComplete() {
                            super.hookOnComplete();
                            log.info("hookOnComplete. Finish getting payment from cache.");
                            counter.incrementAndGet();
                            if (!isPaymentMonoIsEmpty) {
                                payments.add(Map.entry(paymentRequest, paymentResult));
                            } else {
                                payments.add(Map.entry(paymentRequest, null));
                            }

                            if (counter.get() == paymentRequests.size()) {
                                getLastPaymentFromClientService(payments);
                            }
                        }

                        @Override
                        protected void hookOnError(Throwable throwable) {
                            log.error("hookOnError. error getting payment-cache, because of={}", throwable.getMessage());

                            payments.add(Map.entry(paymentRequest, null));

                            if (counter.get() == paymentRequests.size()) {
                                getLastPaymentFromClientService(payments);
                            }
                        }
                    });
        }
    }

    private void checkLastPaymentAsync(LastPaymentResponseDto lastPayment, PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
        log.info("Input checkLastPaymentAsync. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentResult.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        boolean isTrusted = geoAnalyzer.checkPayment(lastPayment, paymentRequest);
        paymentResult.setTrusted(isTrusted);

        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
        savePaymentService.savePayment(isTrusted, savePaymentRequestDto, paymentResult);

        log.info("Output checkLastPaymentAsync. Finish");
    }

    private void getLastPaymentFromClientService(List<Map.Entry<PaymentRequestDto, PaymentResponseDto>> payments) {
        log.info("Input getLastPaymentFromClientService. received map with current payments and payments from cache");

        List<PaymentRequestDto> paymentsList = payments.stream().map(payment -> payment.getKey()).toList();

        clientService
                .getLastPayment(paymentsList)
                .doOnNext(lastPayment -> {
                    log.info("hookOnNext. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} } }",
                            lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate());
//                    checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
                })
                .doOnError(throwable -> {
                    log.error("getLastPaymentFromClientService hookOnError. error from ms-payment, because of {}", throwable.getMessage());

                    if (throwable instanceof NotFoundException) {
//                        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
//                        savePaymentService.savePayment(true, savePaymentRequestDto, paymentResult);
                    }
                })
                .retryWhen(
                        Retry
                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.RETRY_INTERVAL))
                                .filter(throwable -> !(throwable instanceof NotFoundException))
                                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> {
//                                    sendBackoffMessageInKafka(paymentRequest);
                                    throw new RuntimeException("Error getLastPaymentFromClientService. External ms-payment failed to process after max retries");
                                }))
                )
                .subscribe(new BaseSubscriber<LastPaymentResponseDto>() {

                    Subscription subscription;

                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        super.hookOnSubscribe(subscription);
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    protected void hookOnNext(LastPaymentResponseDto value) {
                        super.hookOnNext(value);
                        subscription.request(1);
                    }
                });
    }

    private void sendBackoffMessageInKafka(PaymentRequestDto paymentRequest) {
        try {
            byte[] backoffBytesPayment = objectMapper.writeValueAsBytes(paymentRequest);
            paymentProducer.sendBackoffMessage(String.valueOf(paymentRequest.getId()), backoffBytesPayment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
