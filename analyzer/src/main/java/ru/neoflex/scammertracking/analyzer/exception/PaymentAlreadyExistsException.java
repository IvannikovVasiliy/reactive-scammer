package ru.neoflex.scammertracking.analyzer.exception;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(String message) {
        super(message);
    }
}
