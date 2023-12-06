package ru.neoflex.scammertracking.analyzer.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
