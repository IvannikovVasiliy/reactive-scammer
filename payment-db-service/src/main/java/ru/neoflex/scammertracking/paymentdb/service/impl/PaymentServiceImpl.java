package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.PaymentResponseDto;
import ru.neoflex.scammertracking.paymentdb.domain.dto.SavePaymentRequestDto;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.repository.PaymentRepository;
import ru.neoflex.scammertracking.paymentdb.service.LogService;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

@Service
@Transactional//(isolation = Isolation.READ_COMMITTED)
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    //    private final JdbcTemplate jdbcTemplate;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final LogService logService;

    @Override
    public Mono<PaymentResponseDto> getLastPayment(String cardNumber) {
        //log.info("request. receiver card number={}", cardNumber);

        String errMessage = String.format("Payer card number with id=%s not found", cardNumber);

        Mono<PaymentResponseDto> paymentResponse = paymentRepository
                .findByPayerCardNumber(cardNumber)
                .map(payment ->  modelMapper.map(payment, PaymentResponseDto.class))
                .switchIfEmpty(
                        Mono.error(new PaymentNotFoundException(errMessage))
                )
                .doOnSuccess(paymentResponseDto -> {
                    //System.out.println(paymentResponseDto.getPayerCardNumber());
                });

        return paymentResponse;
    }

    @Override
    public Mono<Void> savePayment(SavePaymentRequestDto payment) {
//        log.info("received. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
//                payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());
//
//        String query = "INSERT INTO payments VALUES(?,?,?,?,?,?)";
//        try {
//            jdbcTemplate.update(query, payment.getId(), payment.getPayerCardNumber(), payment.getReceiverCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());
//        } catch (DuplicateKeyException e) {
//            String errorMessage = String.format("The payment with id=%s is already exist", payment.getId());
//            log.error("error. {}", errorMessage);
//            throw new PaymentAlreadyExistsException(errorMessage);
//        } catch (Exception e) {
//            log.error("error. Cannot be saved the payment: {id={},payerCardNumber={},receiverCardNUmber={},latitude={}, longitude={}, date={} }",
//                    payment.getId(), payment.getCoordinates(), payment.getPayerCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());
//        }
//
//        log.info("response. Save the payment: {id={},payerCardNumber={},receiverCardNUmber={},latitude={}, longitude={}, date={} }",
//                payment.getId(), payment.getCoordinates(), payment.getPayerCardNumber(), payment.getCoordinates().getLatitude(), payment.getCoordinates().getLongitude(), payment.getDate());

        return Mono.empty();
    }

    //    @Override
//    public PaymentResponseDto getLastPayment(Long id) {
//
//        PaymentEntity paymentEntity = jdbcTemplate.queryForObject("SELECT * FROM payments     WHERE id=?", new PaymentRowMapper(), id);
//        PaymentResponseDto paymentResponse = modelMapper.map(paymentEntity, PaymentResponseDto.class);
//        paymentResponse.setCoordinates(new Coordinates(paymentEntity.getLatitude(), paymentEntity.getLongitude()));
//
//        return paymentResponse;
//    }
//
//    @Override
//    public void insertNullPayments(String idCardNumber) {
//        final String INSERT_NULL_PAYMENT = String.format("INSERT INTO payments(id_payment, id_card_number, latitude, longitude, date) VALUES(null, %s, null, null, null);", idCardNumber);
//
//        StringBuffer query = new StringBuffer();
//        query
//                .append("begin work;")
//                .append("lock table payments in row exclusive mode;");
//        for (int i = 0; i < Constants.MAX_COUNT_PAYMENTS_IN_DATABASE; i++) {
//            query.append(INSERT_NULL_PAYMENT);
//        }
//        query.append("end;");
//
//        jdbcTemplate.update(query.toString());
//    }

//    @Override
//    public UpdatePaymentResponseDto updatePayments(UpdatePaymentRequestDto updateRequest) {
//        jdbcTemplate.update("UPDATE TABLE payments SET id_payment=?, id_card_number=?, latitude=?, longitude=?, date=? WHERE id=?",
//                updateRequest.getIdPayment(), updateRequest.getCardNumber(), updateRequest.getCoordinates().getLatitude(), updateRequest.getCoordinates().getLongitude(), updateRequest.getDate(), id);
//
//        UpdatePaymentResponseDto updateResponse = modelMapper.map(updateRequest, UpdatePaymentResponseDto.class);
//
//        return updateResponse;
//    }
}
