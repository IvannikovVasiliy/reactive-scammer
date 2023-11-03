package ru.neoflex.scammertracking.paymentdb.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.neoflex.scammertracking.paymentdb.domain.enums.DbAction;
import ru.neoflex.scammertracking.paymentdb.service.LogService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogServiceImpl implements LogService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertLog(String cardNumber, DbAction action, String queryPayment) {
        log.info("prepare to insert log: {}", queryPayment);

        final String query = "INSERT INTO logs(id_payer_card_number, query, action) VALUES(?, ?, ?)";
        jdbcTemplate.update(query, cardNumber, queryPayment, action.name());

        log.info("insert in database log: {}", queryPayment);
    }
}