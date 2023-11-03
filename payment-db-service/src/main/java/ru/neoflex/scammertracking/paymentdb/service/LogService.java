package ru.neoflex.scammertracking.paymentdb.service;

import ru.neoflex.scammertracking.paymentdb.domain.enums.DbAction;

public interface LogService {
    void insertLog(String cardNumber, DbAction select, String query);
}
