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

    private final PaymentCacheRepository paymentCacheRepository;
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
                        log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
                        this.payment = payment;
                        isPaymentMonoIsEmpty = false;
                    }

                    @Override
                    protected void hookOnComplete() {
                        super.hookOnComplete();
                        log.info("hookOnComplete. Finish getting payment from cache.");
                        if (isPaymentMonoIsEmpty) {
                            getLastPaymentFromClientService(paymentRequest, paymentResult);
                        } else {
                            LastPaymentResponseDto lastPayment = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
                            checkLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
                        }
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        log.error("hookOnError. error getting payment-cache, because of={}", throwable.getMessage());
                        getLastPaymentFromClientService(paymentRequest, paymentResult);
                        // timeout
                    }
                });
    }

    private void checkLastPaymentAsync(LastPaymentResponseDto lastPayment, PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
        log.info("Input checkLastPaymentAsync. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentResult.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        boolean isTrusted = geoAnalyzer.checkPayment(lastPayment, paymentRequest);
        paymentResult.setTrusted(isTrusted);

        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
        routerPayment.routePayment(isTrusted, savePaymentRequestDto, paymentResult);

        log.info("Output checkLastPaymentAsync. Finish");
    }

    private void getLastPaymentFromClientService(PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
        log.info("Input getLastPaymentFromClientService. paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }, paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentResult.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        clientService
                .getLastPayment(paymentRequest)
                .subscribe(new BaseSubscriber<>() {

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
                            // timeout
                        }
                    }
                });
    }
}
