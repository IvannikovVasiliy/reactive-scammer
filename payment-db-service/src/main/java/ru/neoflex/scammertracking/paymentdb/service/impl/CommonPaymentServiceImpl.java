package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.paymentdb.service.CommonPaymentService;
import ru.neoflex.scammertracking.paymentdb.service.PaymentBufferService;
import ru.neoflex.scammertracking.paymentdb.service.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonPaymentServiceImpl implements CommonPaymentService {

    private final PaymentService paymentService;
    private final PaymentBufferService bufferService;

//    @Override
//    public PaymentResponseDto getLastPayment(GetLastPaymentRequestDto getLastPaymentRequestDto) {
//        LOGGER.info("request. receiver card number={}", getLastPaymentRequestDto.getPayerCardNumber());
//
//        String payerCardNumber = getLastPaymentRequestDto.getPayerCardNumber();
//        String query = "select * from payments where payer_card_number=? and date=(select max(date) from payments where payer_card_number=?)";
//        PaymentEntity paymentEntity = null;
//        try {
//            paymentEntity = jdbcTemplate.queryForObject(query, new PaymentRowMapper(), payerCardNumber, payerCardNumber);
//        } catch (Exception e) {
//            LOGGER.warn("Payer card number with id={} not found", payerCardNumber);
//            String errorMessage = String.format("Payer card number with id=%s not found", payerCardNumber);
//            throw new PaymentNotFoundException(errorMessage);
//        }
//
//        PaymentResponseDto response = modelMapper.map(paymentEntity, PaymentResponseDto.class);
//        response.setCoordinates(new Coordinates(paymentEntity.getLatitude(), paymentEntity.getLongitude()));
//
//        LOGGER.info("received. lastPayment={ id={}, payerCardNumber={}, receiverCardNumber={}, latitude={}, longitude={}, date ={} }",
//                response.getId(), response.getPayerCardNumber(), response.getReceiverCardNumber(), response.getCoordinates().getLatitude(), response.getCoordinates().getLongitude(), response.getDate());
//
//        return response;
//    }


//    @Override
//    @Transactional(isolation = Isolation.READ_COMMITTED)
//    public PaymentResponseDto getLastPayment(String cardNumber) {
//        PaymentBufferEntity buffer = bufferService.getPaymentByCardNumber(cardNumber);
//        PaymentResponseDto paymentResponse = paymentService.getLastPayment(buffer.getIdPkPayment());
//
//        return paymentResponse;
//    }

//    @Override
//    public void insertPayments(String idCardNumber) {
//        paymentService.insertNullPayments(idCardNumber);
//        bufferService.insertPaymentBuffer(idCardNumber);
//    }
//
//    @Override
//    public UpdatePaymentResponseDto updatePayments(UpdatePaymentRequestDto updatePaymentRequest) {
//        paymentService.getLastPayment()
//    }
}
