package ru.neoflex.scammertracking.paymentdb.advice;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.neoflex.scammertracking.paymentdb.domain.dto.MessageInfoDto;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.error.valid.ValidationErrorResponse;
import ru.neoflex.scammertracking.paymentdb.error.valid.Violation;
import ru.neoflex.scammertracking.paymentdb.utils.Constants;

import java.util.List;

@RestControllerAdvice
public class ErrorHandler {

    MessageInfoDto messageInfo = new MessageInfoDto(-1);

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public MessageInfoDto handlePaymentNotFound(PaymentNotFoundException paymentNotFound) {
        messageInfo.setRespCode(Constants.NOT_FOUND);
        messageInfo.setMessage(paymentNotFound.getMessage());
        return messageInfo;
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public MessageInfoDto handlePaymentNotFound(PaymentAlreadyExistsException resourceExists) {
        messageInfo.setRespCode(Constants.BAD_REQUEST);
        messageInfo.setMessage(resourceExists.getMessage());
        return messageInfo;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleConstraintValidation(ConstraintViolationException e) {
        List<Violation> violations = e.getConstraintViolations()
                .stream()
                .map(violation ->
                        new Violation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        return new ValidationErrorResponse(violations);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<Violation> violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new Violation(error.getField(), error.getDefaultMessage()))
                .toList();

        return new ValidationErrorResponse(violations);
    }
}