package ru.neoflex.scammertracking.analyzer.router.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.error.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.router.RouterPayment;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouterPaymentImpl implements RouterPayment {

    private final ClientService clientService;
    private final SourceMapperImplementation sourceMapper;
    private final PaymentProducer paymentProducer;
    private final PaymentCacheRepository paymentCacheRepository;
    private final ObjectMapper objectMapper;

    public void routePayment(boolean isTrusted, SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult) {
        log.info("Received. isTrusted={}, isCacheDeprecated={ }. PaymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n Payment result={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {}}",
                isTrusted, savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        paymentResult.setTrusted(isTrusted);
        if (isTrusted) {
            clientService
                    .savePayment(savePaymentRequest)
                    .subscribe(new BaseSubscriber<>() {
                        @Override
                        protected void hookOnSubscribe(Subscription subscription) {
                            super.hookOnSubscribe(subscription);
                            log.info("subscribe on save payment");
                        }

                        @Override
                        protected void hookOnComplete() {
                            super.hookOnComplete();
                            log.info("Complete request to save payment to ms-payment");
                            paymentCacheRepository
                                    .findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber())
                                    .subscribe(new BaseSubscriber<>() {

                                        PaymentEntity payment = null;

                                        @Override
                                        protected void hookOnSubscribe(Subscription subscription) {
                                            super.hookOnSubscribe(subscription);
                                            log.info("hookOnSubscribe. Subscribe to payment-service for saving payment");
                                        }

                                        @Override
                                        protected void hookOnNext(PaymentEntity payment) {
                                            super.hookOnNext(payment);
                                            log.info("hookOnNext. get payment from cache. payment={ payerCardNumber={}, receiverCardNumber={}, idPayment={}, latitude={}, longitude={}, datePayment={} }",
                                                    payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment());
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
                                                        .subscribe(new BaseSubscriber<>() {
                                                            @Override
                                                            protected void hookOnComplete() {
                                                                super.hookOnComplete();
                                                                paymentCacheRepository
                                                                        .expire()
                                                                        .doOnError(error -> log.error("Error. Expiration date not setted to payment cache"))
                                                                        .subscribe();
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        protected void hookOnError(Throwable throwable) {
                                            log.error("hookOnError. Error saving payment in redis, because of={}", throwable.getMessage());
                                        }
                                    });
                            paymentProducer.sendCheckedMessage(paymentResult);
                        }

                        @Override
                        protected void hookOnError(Throwable throwable) {
                            if (throwable instanceof BadRequestException) {
                                byte[] paymentResultBytes = new byte[0];
                                try {
                                    paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
                                } catch (JsonProcessingException e) {
                                    log.error("Unable to parse paymentResult into bytes");
                                    throw new RuntimeException(e);
                                }
                                paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
                                log.info("Response. Sent message in suspicious-topic, BadRequest because of {}",
                                        throwable.getMessage());
                            } else {
                                // timeout
                            }
                        }
                    });
        } else {
            byte[] paymentResultBytes = new byte[0];
            try {
                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse paymentResult into bytes");
                throw new RuntimeException(e);
            }
            paymentProducer.sendSuspiciousMessage(paymentResult.getPayerCardNumber(), paymentResultBytes);
            log.info("Response. Sent message in suspicious-topic, because latitude={}, longitude={}",
                    savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude());
        }
    }
}
