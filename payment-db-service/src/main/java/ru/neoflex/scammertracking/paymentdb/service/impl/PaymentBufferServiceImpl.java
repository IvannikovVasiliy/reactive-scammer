package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentBufferEntity;
import ru.neoflex.scammertracking.paymentdb.service.PaymentBufferService;

@Service
@RequiredArgsConstructor
public class PaymentBufferServiceImpl implements PaymentBufferService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public PaymentBufferEntity getPaymentByCardNumber(String cardNumber) {
        return null;
    }

    @Override
    public void insertPaymentBuffer(String idCardNumber) {

    }

    //    @Override
//    public PaymentBufferEntity getPaymentByCardNumber(String cardNumber) {
//        PaymentBufferEntity buffer = jdbcTemplate.queryForObject("select * from payment_buffer where id_card_number=?", PaymentBufferEntity.class, cardNumber);
//
//        return buffer;
//    }
//
//    @Override
//    public void insertPaymentBuffer(String idCardNumber) {
//        jdbcTemplate.update("INSERT INTO payments_buffer(id_pk_payment, id_card_number, offset_pos) VALUES(null, ?, 0)", idCardNumber);
//    }
}
