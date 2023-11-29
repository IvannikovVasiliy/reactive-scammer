package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.neoflex.scammertracking.paymentdb.domain.dto.EditPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.GetLastPaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;
import ru.neoflex.scammertracking.paymentdb.error.exception.DatabaseInternalException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.repository.PaymentRepository;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;
import ru.neoflex.scammertracking.paymentdb.utils.Constants;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional//(isolation = Isolation.READ_COMMITTED)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public Flux<Map.Entry<GetLastPaymentRequestDto, Optional<PaymentResponseDto>>> getLastPayment(List<GetLastPaymentRequestDto> paymentRequests) {
        log.info("request getLastPayment. receive list of paymentRequests. size of list = {}", paymentRequests.size());

        List<PaymentResponseDto> paymentResponseList = new ArrayList<>();

        return Flux
                .fromIterable(paymentRequests)
                .flatMap(paymentRequestDto -> {
                    log.info("flatMap. paymentRequestDto with id={}", paymentRequestDto);
                    String cardNumber = paymentRequestDto.getCardNumber();
                    String errMessage = String.format("Payer card number with id=%s not found", cardNumber);

                    PaymentResponseDto paymentResp = null;

                    PaymentResponseDto paymentResponse = null;
                    return paymentRepository
                            .findByPayerCardNumber(cardNumber)
                            .map(payment -> {
                                PaymentResponseDto paymentResponseDto = modelMapper.map(payment, PaymentResponseDto.class);
                                paymentResponseDto.setCoordinates(new Coordinates(payment.getLatitude(), payment.getLongitude()));
                                return Map.entry(paymentRequestDto, Optional.of(paymentResponseDto));
                            })
                            .switchIfEmpty(Mono.just(Map.entry(paymentRequestDto, Optional.empty())));
//                            .retryWhen(
//                                    Retry
//                                            .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.INTERVAL_COUNT))
//                                            .filter(throwable -> !(throwable instanceof PaymentNotFoundException))
//                                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
//                                                throw new DatabaseInternalException("Database internal exception");
//                                            })
//                            );
//                            .subscribe(new BaseSubscriber<PaymentResponseDto>() {
//                                @Override
//                                protected void hookOnNext(PaymentResponseDto value) {
//                                    super.hookOnNext(value);
////                                    paymentResponseList.add(value);
//                                }
//                            });
                });

//        return Flux.fromIterable(paymentResponseList);
    }

//        Mono<PaymentResponseDto> paymentResponse = paymentRepository
//                .findByPayerCardNumber(cardNumber)
//                .map(payment -> {
//                    PaymentResponseDto paymentResponseDto = modelMapper.map(payment, PaymentResponseDto.class);
//                    paymentResponseDto.setCoordinates(new Coordinates(payment.getLatitude(), payment.getLongitude()));
//                    return paymentResponseDto;
//                })
//                .switchIfEmpty(Mono.error(new PaymentNotFoundException(errMessage)))
//                .retryWhen(
//                        Retry
//                                .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.INTERVAL_COUNT))
//                                .filter(throwable -> !(throwable instanceof PaymentNotFoundException))
//                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
//                                    throw new DatabaseInternalException("Database internal exception");
//                                })
//                );
//
//        return paymentResponse;
//                }

        @Override
        public Mono<Void> savePayment (SavePaymentRequestDto payment){
            log.info("received. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                    payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());

            PaymentEntity paymentEntity = modelMapper.map(payment, PaymentEntity.class);
            paymentEntity.setLatitude(payment.getCoordinates().getLatitude());
            paymentEntity.setLongitude(payment.getCoordinates().getLongitude());
            paymentEntity.setDate(payment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

            return paymentRepository
                    .insert(paymentEntity)
                    .doOnError(error -> {
                        if (error instanceof DuplicateKeyException) {
                            String errorMessage = String.format("The payment with id=%s is already exist", payment.getId());
                            log.error("error. {}", errorMessage);
                            throw new PaymentAlreadyExistsException(errorMessage);
                        } else {
                            log.error("error. Cannot be saved the payment: {id={},payerCardNumber={},receiverCardNUmber={},latitude={}, longitude={}, date={} }",
                                    payment.getId(), payment.getCoordinates(), payment.getPayerCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());
                            throw new RuntimeException(error);
                        }
                    })
                    .retryWhen(
                            Retry
                                    .fixedDelay(Constants.RETRY_COUNT, Duration.ofSeconds(Constants.INTERVAL_COUNT))
                                    .filter(throwable -> !(throwable instanceof PaymentAlreadyExistsException))
                                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                        throw new DatabaseInternalException("Database internal exception");
                                    })
                    )
                    .then();
        }

        @Override
        public Mono<Void> putPayment (EditPaymentRequestDto editPaymentRequest){
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
        public Mono<Void> deletePaymentById (Long id){
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
