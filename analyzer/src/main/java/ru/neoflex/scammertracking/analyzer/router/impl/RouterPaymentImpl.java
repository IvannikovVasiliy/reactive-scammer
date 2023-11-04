package ru.neoflex.scammertracking.analyzer.router.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.error.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapper;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.router.RouterPayment;
import ru.neoflex.scammertracking.analyzer.service.PaymentService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouterPaymentImpl implements RouterPayment {

    private final ClientService clientService;
    private final SourceMapperImplementation sourceMapper;
    private final PaymentProducer paymentProducer;
    private final PaymentCacheRepository paymentCacheRepository;

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;
    @Value("${spring.kafka.topic.suspicious-payments}")
    private String suspiciousPaymentsTopic;
    @Value("${spring.kafka.topic.checked-payments}")
    private String checkedPaymentsTopic;

    public void routePayment(boolean isTrusted/*, AtomicBoolean isCacheDeprecated*/, /*PaymentRequestDto*/ SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult) {
        log.info("Received. isTrusted={}, isCacheDeprecated={ }. PaymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n Payment result={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {}}",
                isTrusted, /*isCacheDeprecated,*/ savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

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
                            paymentProducer.sendMessage(checkedPaymentsTopic, paymentResult);
                        }

                        @Override
                        protected void hookOnError(Throwable throwable) {
                            if (throwable instanceof BadRequestException) {
                                paymentProducer.sendMessage(suspiciousPaymentsTopic, paymentResult);
                                log.info("Response. Sent message in topic={}, BadRequest because of {}",
                                        suspiciousPaymentsTopic, throwable.getMessage());
                            }
                        }
                    });
        } else {
            paymentProducer.sendMessage(suspiciousPaymentsTopic, paymentResult);
            log.info("Response. Sent message in topic={}, because latitude={}, longitude={}",
                    suspiciousPaymentsTopic, savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude());
        }
    }
}
