package ru.neoflex.scammertracking.analyzer.exception;

public class SuspiciousPaymentException extends RuntimeException {

    public SuspiciousPaymentException(String errorMessage) {
        super(errorMessage);
    }
}
