package ru.neoflex.scammertracking.paymentdb.error.exception;

public class DatabaseInternalException extends RuntimeException {

    public DatabaseInternalException(String message) {
        super(message);
    }
}
