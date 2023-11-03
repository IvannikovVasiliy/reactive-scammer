package ru.neoflex.scammertracking.paymentdb.map;

import org.springframework.jdbc.core.RowMapper;
import ru.neoflex.scammertracking.paymentdb.domain.entity.PaymentEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class PaymentRowMapper implements RowMapper<PaymentEntity> {

    @Override
    public PaymentEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(rs.getLong("id"));
        paymentEntity.setPayerCardNumber(rs.getString("payer_card_number"));
        paymentEntity.setReceiverCardNumber(rs.getString("receiver_card_number"));
        paymentEntity.setLatitude(rs.getFloat("latitude"));
        paymentEntity.setLongitude(rs.getFloat("longitude"));
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
        paymentEntity.setDate(date);

        return paymentEntity;
    }
}
