package ru.neoflex.scammertracking.analyzer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.analyzer.check.CheckRequest;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.geo.GeoAnalyzer;
import ru.neoflex.scammertracking.analyzer.kafka.producer.PaymentProducer;
import ru.neoflex.scammertracking.analyzer.mapper.ByteArrayMapper;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.router.RouterPayment;
import ru.neoflex.scammertracking.analyzer.service.PaymentPreAnalyzer;
import ru.neoflex.scammertracking.analyzer.service.PaymentService;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentPreAnalyzerImpl implements PaymentPreAnalyzer {

    //    private final FeignService feignService;
    private final PaymentService paymentService;
    private final PaymentCacheRepository paymentCacheRepository;
    private final PaymentProducer paymentProducer;
    //    private final ModelMapper modelMapper;
    private final SourceMapperImplementation sourceMapper;
    private final GeoAnalyzer geoAnalyzer;
    private final CheckRequest checkRequest;
    private final ClientService clientService;
    private final RouterPayment routerPayment;
    private final ByteArrayMapper byteArrayMapper;
    private final ObjectMapper objectMapper;

    @Value("${hostPort.paymentService}")
    private String paymentServiceHostPort;
    @Value("${spring.kafka.topic.suspicious-payments}")
    private String suspiciousPaymentsTopic;
    @Value("${spring.kafka.topic.checked-payments}")
    private String checkedPaymentsTopic;

    @Override
    public void preAnalyzeConsumeMessage(String key, PaymentRequestDto paymentRequest) {
        log.info("received key={} paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                key, paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

        PaymentResponseDto paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);

        boolean isPreCheckSuspicious = checkRequest.preCheckSuspicious(paymentRequest);
        if (isPreCheckSuspicious) {
            log.info("response. Sent message with key={} in topic={}", key, suspiciousPaymentsTopic);
            paymentResult.setTrusted(false);
            byte[] paymentResultBytes = new byte[0];
            try {
                paymentResultBytes = objectMapper.writeValueAsBytes(paymentResult);
            } catch (JsonProcessingException e) {
                log.error();
                throw new RuntimeException(e);
            }
            paymentProducer.sendSuspiciousMessage(key, paymentResultBytes);
            return;
        }

        paymentService.processLastPayment(paymentRequest);

//        paymentCacheRepository
//                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
//                .subscribe(new BaseSubscriber<>() {
//
//                    PaymentEntity payment = null;
//                    boolean isPaymentMonoIsEmpty = true;
//
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        log.info("hookOnSubscribe. Subscribe on mono with payment in cache");
//                    }
//
//                    @Override
//                    protected void hookOnNext(PaymentEntity payment) {
//                        super.hookOnNext(payment);
//                        log.info("hookOnNext. payment = { payerCardNumber={}, receiverCardNumber={}, idPaymen={}, latitude={}, longitude={}, datePayment={}, dateUpdating={} }",
//                                payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getIdPayment(), payment.getLatitude(), payment.getLongitude(), payment.getDatePayment(), payment.getDateUpdating());
//                        this.payment = payment;
//                        isPaymentMonoIsEmpty = false;
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
//                        if (isPaymentMonoIsEmpty) {
//                            clientService
//                                    .getLastPayment(paymentRequest)
//                                    .subscribe(new BaseSubscriber<LastPaymentResponseDto>() {
//                                        @Override
//                                        protected void hookOnSubscribe(Subscription subscription) {
//                                            super.hookOnSubscribe(subscription);
//                                            log.info();
//                                        }
//
//                                        @Override
//                                        protected void hookOnNext(LastPaymentResponseDto value) {
//                                            super.hookOnNext(value);
//                                        }
//
//                                        @Override
//                                        protected void hookOnComplete() {
//                                            super.hookOnComplete();
//                                        }
//
//                                        @Override
//                                        protected void hookOnError(Throwable throwable) {
//                                            super.hookOnError(throwable);
//                                        }
//                                    });
//                        } else {
//                            LastPaymentResponseDto lastPayment = sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(payment);
//                            getLastPaymentAsync(lastPayment, paymentRequest, paymentResult);
//                        }
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        log.error("hookOnError. error getting payment-cache");
//                        super.hookOnError(throwable);
//                    }
//                });

//        Mono<LastPaymentResponseDto> lastPayment = null;
////        try {
//        lastPayment = paymentService.getLastPayment(paymentRequest);
////        } catch (BadRequestException /*|| NotFoundException*/ e) {
////            //modelMapper.map(paymentRequest, PaymentResponseDto.class);
////            paymentResult = sourceMapper.sourceFromPaymentRequestDtoToPaymentResponseDto(paymentRequest);
////            paymentResult.setTrusted(false);
////            paymentProducer.sendMessage(suspiciousPaymentsTopic, paymentResult);
////            log.warn("sent message with key={} in topic {}", key, suspiciousPaymentsTopic);
////            return;
////        }
//
//        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(paymentRequest);
//        PaymentResponseDto paymentResponseDto = new PaymentResponseDto(paymentResult);
//        lastPayment
////                .doOnNext(payment ->
////                        getLastPaymentAsync(payment, paymentRequestDto, paymentResponseDto))
////                .doOnError(err -> {
////                    if (err instanceof NotFoundException) {
////                        SavePaymentRequestDto savePaymentRequestDto = sourceMapper.sourceFromPaymentRequestDtoToSavePaymentRequestDto(paymentRequestDto);
////                        routePayment(true, savePaymentRequestDto, paymentResponseDto);
////                    }
////                })
//                .subscribe(new BaseSubscriber<LastPaymentResponseDto>() {
//
//                    int count = 0;
//                    LastPaymentResponseDto lastPaymentResponse = null;
//
//                    @Override
//                    protected void hookOnSubscribe(Subscription subscription) {
//                        super.hookOnSubscribe(subscription);
//                        System.out.println("s");
//                    }
//
//                    @Override
//                    protected void hookOnNext(LastPaymentResponseDto value) {
//                        super.hookOnNext(value);
//                        count++;
//                        lastPaymentResponse = value;
//                        System.out.println("n");
//                    }
//
//                    @Override
//                    protected void hookOnComplete() {
//                        super.hookOnComplete();
////                        System.out.println("f");
//                        if
//                    }
//
//                    @Override
//                    protected void hookOnError(Throwable throwable) {
//                        super.hookOnError(throwable);
//                    }
//                });
    }
}
