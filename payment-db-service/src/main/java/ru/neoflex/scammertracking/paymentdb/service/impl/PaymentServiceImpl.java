package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.paymentdb.domain.dto.*;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;
import ru.neoflex.scammertracking.paymentdb.error.exception.DatabaseInternalException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.map.SourceMapper;
import ru.neoflex.scammertracking.paymentdb.map.impl.SourceMapperImplementation;
import ru.neoflex.scammertracking.paymentdb.repository.PaymentRepository;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;
import ru.neoflex.scammertracking.paymentdb.utils.Constants;

import java.time.Duration;
import java.time.ZoneId;

@Service
@Transactional//(isolation = Isolation.READ_COMMITTED)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final SourceMapperImplementation sourceMapper;

    @Override
    public Flux<AggregateLastPaymentDto> getLastPayment(Flux<AggregateLastPaymentDto> paymentRequests) {
        log.info("request getLastPayment. receive flux of paymentRequests");

        return paymentRequests
                .flatMap(paymentRequestDto -> {
                    log.info("flatMap. paymentRequestDto with id={}", paymentRequestDto);
                    String cardNumber = paymentRequestDto.getPaymentRequest().getPayerCardNumber();
                    String errMessage = String.format("Payer card number with id=%s not found", cardNumber);

                    PaymentResponseDto paymentResp = null;

                    PaymentResponseDto paymentResponse = null;
//                    return Mono.error(new PaymentNotFoundException("not found payment"));
                    return paymentRepository
                            .findByPayerCardNumber(cardNumber)
                            .map(payment -> {
                                PaymentResponseDto paymentResponseDto = modelMapper.map(payment, PaymentResponseDto.class);
                                paymentResponseDto.setCoordinates(new Coordinates(payment.getLatitude(), payment.getLongitude()));
                                return new AggregateLastPaymentDto(paymentRequestDto.getPaymentRequest(), paymentResponseDto);
                            })
                            .switchIfEmpty(Mono.just(new AggregateLastPaymentDto(paymentRequestDto.getPaymentRequest(), null)));
                });
    }

    @Override
    public Flux<SavePaymentResponseDto> savePayment(Flux<SavePaymentRequestDto> payment) {
//            log.info("received. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
//                    payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());

        return payment
                .flatMap(p -> {
                    System.out.println("get payment from ");
                    PaymentEntity paymentEntity = modelMapper.map(p, PaymentEntity.class);
                    paymentEntity.setLatitude(p.getCoordinates().getLatitude());
                    paymentEntity.setLongitude(p.getCoordinates().getLongitude());
                    paymentEntity.setDate(p.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

                    return paymentRepository
                            .insert(paymentEntity)
                            .then(Mono.defer(() -> Mono.just(sourceMapper.sourceFromPaymentEntityToSavePaymentResponseDto(paymentEntity))))
                            .doOnError(error -> {
                                if (error instanceof DuplicateKeyException) {
                                    String errorMessage = String.format("The payment with id=%s is already exist", p.getId());
                                    log.error("error. {}", errorMessage);
                                    throw new PaymentAlreadyExistsException(errorMessage);
                                } else {
                                    log.error("error. Cannot be saved the payment: {id={},payerCardNumber={},receiverCardNUmber={},latitude={}, longitude={}, date={} }",
                                            p.getId(), p.getCoordinates(), p.getPayerCardNumber(), p.getCoordinates().getLatitude(), p.getCoordinates().getLongitude(), p.getDate());
                                    throw new RuntimeException(error);
                                }
                            });

//                    return Mono.just(savePaymentResponse);
                });
                //.delayElements(Duration.ofSeconds(1));
    }

    @Override
    public Mono<Void> putPayment(EditPaymentRequestDto editPaymentRequest) {
        log.debug("received putPayment. editPaymentRequest with id={}", editPaymentRequest.getId());

        PaymentEntity paymentEntity = modelMapper.map(editPaymentRequest, PaymentEntity.class);
        paymentEntity.setLatitude(editPaymentRequest.getCoordinates().getLatitude());
        paymentEntity.setLongitude(editPaymentRequest.getCoordinates().getLongitude());
        paymentEntity.setDate(editPaymentRequest.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

        return paymentRepository
                .save(paymentEntity)
                .retryWhen(
                        Retry
                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.INTERVAL_COUNT))
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    throw new DatabaseInternalException("Database internal exception");
                                })
                )
                .then();
    }

    @Override
    public Mono<Void> deletePaymentById(Long id) {
        log.info("received deletePayment. id={}", id);

        return paymentRepository
                .deleteById(id)
                .retryWhen(
                        Retry
                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.INTERVAL_COUNT))
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                    throw new DatabaseInternalException("Database internal exception");
                                })
                );
    }
}
