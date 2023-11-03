package ru.neoflex.scammertracking.paymentdb.error.exception;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
