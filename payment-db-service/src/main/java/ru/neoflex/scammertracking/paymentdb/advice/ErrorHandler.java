package ru.neoflex.scammertracking.paymentdb.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.MessageInfoDto;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.error.valid.ValidationErrorResponse;
import ru.neoflex.scammertracking.paymentdb.error.valid.Violation;
import ru.neoflex.scammertracking.paymentdb.utils.Constants;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final int ERROR_CODE = -1;
    MessageInfoDto messageInfo = new MessageInfoDto(ERROR_CODE);

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public Mono<MessageInfoDto> handlePaymentNotFound(PaymentNotFoundException paymentNotFound) {
        log.info("Input handlePaymentNotFound. {}", paymentNotFound.getMessage());

        messageInfo.setRespCode(Constants.NOT_FOUND);
        messageInfo.setMessage(paymentNotFound.getMessage());

        log.error("Output handlePaymentNotFound. messageInfo={ errorCode={}, respCode={}, message={} }",
                messageInfo.getErrorCode(), messageInfo.getRespCode(), messageInfo.getMessage());
        return Mono.just(messageInfo);
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Mono<MessageInfoDto> handlePaymentNotFound(PaymentAlreadyExistsException resourceExists) {
        messageInfo.setRespCode(Constants.BAD_REQUEST);
        messageInfo.setMessage(resourceExists.getMessage());
        return Mono.just(messageInfo);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ValidationErrorResponse> handleConstraintValidation(ConstraintViolationException e) {
        List<Violation> violations = e.getConstraintViolations()
                .stream()
                .map(violation ->
                        new Violation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return Mono.just(new ValidationErrorResponse(violations));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<Violation> violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .toList();

        return Mono.just(new ValidationErrorResponse(violations));
    }
}