package ru.neoflex.scammertracking.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.analyzer.client.ClientService;
import ru.neoflex.scammertracking.analyzer.repository.PaymentCacheRepository;
import ru.neoflex.scammertracking.analyzer.domain.dto.LastPaymentResponseDto;
import ru.neoflex.scammertracking.analyzer.domain.dto.PaymentRequestDto;
import ru.neoflex.scammertracking.analyzer.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.analyzer.mapper.SourceMapperImplementation;
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

    @Override
    public Mono<LastPaymentResponseDto> getLastPayment(PaymentRequestDto paymentRequest) {
        log.info("Input getLastPayment. paymentRequest={ } paymentRequest={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                paymentRequest.getId(), paymentRequest.getPayerCardNumber(), paymentRequest.getReceiverCardNumber(), paymentRequest.getCoordinates().getLatitude(), paymentRequest.getCoordinates().getLongitude(), paymentRequest.getDate());

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentCacheRepository
                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
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
//                })
                .doOnNext(p ->
                        System.out.println(p))
                .subscribe();

        return paymentCacheRepository
                .findPaymentByCardNumber(paymentRequest.getPayerCardNumber())
                .flatMap(paymentCacheEntity -> {
                    Mono<LastPaymentResponseDto> lastPaymentResponse = null;
                    if (null != paymentCacheEntity) {
                        lastPaymentResponse = Mono.just(
                                sourceMapper.sourceFromPaymentEntityToLastPaymentResponseDto(paymentCacheEntity)
                        );
                        log.info("Response cache. Last payment response from payment-ms.");
                    } else {
                        lastPaymentResponse = clientService.getLastPayment(paymentRequest);
                        log.info("Response. Cache does not exist. WebClient return last payment response.");
                    }

                    return lastPaymentResponse;
                });
    }
}
