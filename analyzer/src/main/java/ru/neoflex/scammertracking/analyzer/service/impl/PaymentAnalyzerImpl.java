package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.error.exception.BadRequestException;
import ru.neoflex.scammertracking.analyzer.error.exception.NotFoundException;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.service.PaymentAnalyzer;
import ru.neoflex.scammertracking.analyzer.service.PaymentService;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentAnalyzerImpl implements PaymentAnalyzer {

    //    private final FeignService feignService;
    private final PaymentService paymentService;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PaymentProducer paymentProducer;
    //    private final ModelMapper modelMapper;
    private final SourceMapperImplementation sourceMapper;
    private final GeoAnalyzer geoAnalyzer;
    private final CheckRequest checkRequest;
    private final ClientService clientService;

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;
    @Value("${spring.kafka.topic.suspicious-payments}")
    private String suspiciousPaymentsTopic;
    @Value("${spring.kafka.topic.checked-payments}")
    private String checkedPaymentsTopic;

    @Override
    public void analyzeConsumeMessage(String key, PaymentRequestDto paymentRequest) throws Exception {
        log.info("received key={} paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                key, paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

        PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);

        boolean isPreCheckSuspicious = checkRequest.preCheckSuspicious(paymentRequest);
        if (isPreCheckSuspicious) {
            log.info("response. Sent message with key={} in topic={}", key, suspiciousPaymentsTopic);
            paymentResult.setTrusted(false);
            paymentProducer.sendMessage(suspiciousPaymentsTopic, paymentResult);
            return;
        }

        Mono<LastPaymentResponseDto> lastPayment = null;
//        try {
        lastPayment = paymentService.getLastPayment(paymentRequest);
//        } catch (BadRequestException /*|| NotFoundException*/ e) {
//            //modelMapper.map(paymentRequest, PaymentResponseDto.class);
//            paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
//            paymentResult.setTrusted(false);
//            paymentProducer.sendMessage(suspiciousPaymentsTopic, paymentResult);
//            log.warn("sent message with key={} in topic {}", key, suspiciousPaymentsTopic);
//            return;
//        }

        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(paymentRequest);
        PaymentResponseDto paymentResponseDto = new PaymentResponseDto(paymentResult);
        lastPayment
                .doOnNext(payment ->
                        getLastPaymentAsync(payment, paymentRequestDto, paymentResponseDto))
                .doOnError(err -> {
                    if (err instanceof NotFoundException) {
                        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequestDto);
                        routePayment(true, savePaymentRequestDto, paymentResponseDto);
                    }
                })
                .subscribe(val ->
                        System.out.println(val));
    }

    private void getLastPaymentAsync(LastPaymentResponseDto lastPayment, PaymentRequestDto paymentRequest, PaymentResponseDto paymentResult) {
        log.info("Input getLastPaymentAsync. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }; paymentResult={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {} }",
                lastPayment.getId(), lastPayment.getPayerCardNumber(), lastPayment.getReceiverCardNumber(), lastPayment.getCoordinates().getLatitude(), lastPayment.getCoordinates().getLongitude(), lastPayment.getDate(), paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        boolean isTrusted = geoAnalyzer.checkPayment(lastPayment, paymentRequest);
        paymentResult.setTrusted(isTrusted);

        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequest);
        routePayment(isTrusted, savePaymentRequestDto, paymentResult);

        log.info("Output getLastPaymentAsync. Finish");
    }

    private void routePayment(boolean isTrusted/*, AtomicBoolean isCacheDeprecated*/, /*PaymentRequestDto*/ SavePaymentRequestDto savePaymentRequest, PaymentResponseDto paymentResult) {
        log.info("Received. isTrusted={}, isCacheDeprecated={ }. PaymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }.\n Payment result={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={}, trusted = {}}",
                isTrusted, /*isCacheDeprecated,*/ savePaymentRequest.getId(), savePaymentRequest.getPayerCardNumber(), savePaymentRequest.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), savePaymentRequest.getCoordinates().getLongitude(), savePaymentRequest.getDate(), paymentResult.getId(), paymentResult.getPayerCardNumber(), paymentResult.getReceiverCardNumber(), savePaymentRequest.getCoordinates().getLatitude(), paymentResult.getCoordinates().getLongitude(), paymentResult.getDate(), paymentResult.getTrusted());

        if (isTrusted) {
            clientService
                    .savePayment(savePaymentRequest)
                    .subscribe(new BaseSubscriber<Void>() {
                        @Override
                        protected void hookOnSubscribe(Subscription subscription) {
                            super.hookOnSubscribe(subscription);
                            log.info("subscribe on save payment");
                        }

                        @Override
                        protected void hookOnComplete() {
                            super.hookOnComplete();
                            log.info("Complete request to save payment to ms-payment");
                            boolean isCacheEmpty = paymentCacheRepository.findPaymentByCardNumber(savePaymentRequest.getPayerCardNumber()) == null;
                            if (isCacheEmpty) {
                                PaymentEntity paymentEntity = sourceMapper.sourceFromSavePaymentRequestDtoToPaymentEntity(savePaymentRequest);
                                paymentEntity.setDateUpdating(LocalDateTime.now());
                                paymentCacheRepository.save(paymentEntity);
                            }
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
