package ru.neoflex.scammertracking.analyzer.exception;

public class ConnectionRefusedException extends RuntimeException {

    public ConnectionRefusedException(String message) {
        super(message);
    }

    public ConnectionRefusedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "NotFoundException: " + getMessage();
    }

}
