package ru.neoflex.scammertracking.paymentdb.error.exception;

public class PaymentAlreadyExistsException extends RuntimeException {

    public PaymentAlreadyExistsException(String message, Long correlationId) {
        super(message);
        this.correlationId = correlationId;
    }

    public PaymentAlreadyExistsException(String message) {
        super(message);
    }

    private Long correlationId;

    public Long getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Long correlationId) {
        this.correlationId = correlationId;
    }
}
