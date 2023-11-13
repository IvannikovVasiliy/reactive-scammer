package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;
import ru.neoflex.scammertracking.paymentdb.domain.model.Coordinates;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.repository.PaymentRepository;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

import java.time.ZoneId;

@Service
@Transactional//(isolation = Isolation.READ_COMMITTED)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public Mono<PaymentResponseDto> getLastPayment(String cardNumber) {
        log.info("request. receiver card number={}", cardNumber);

        String errMessage = String.format("Payer card number with id=%s not found", cardNumber);

        Mono<PaymentResponseDto> paymentResponse = paymentRepository
                .findByPayerCardNumber(cardNumber)
                .map(payment -> {
                    PaymentResponseDto paymentResponseDto = modelMapper.map(payment, PaymentResponseDto.class);
                    paymentResponseDto.setCoordinates(new Coordinates(payment.getLatitude(), payment.getLongitude()));
                    return paymentResponseDto;
                })
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(errMessage)));

        return paymentResponse;
    }

    @Override
    public Mono<Void> savePayment(SavePaymentRequestDto payment) {
        log.info("received. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
                payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());

        PaymentEntity paymentEntity = modelMapper.map(payment, PaymentEntity.class);
        paymentEntity.setLatitude(payment.getCoordinates().getLatitude());
        paymentEntity.setLongitude(payment.getCoordinates().getLongitude());
        paymentEntity.setDate(payment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return paymentRepository
                .save(paymentEntity)
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
                .then(Mono.empty());
    }
}
