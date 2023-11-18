package ru.neoflex.scammertracking.paymentdb.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionException;
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

    MessageInfoDto messageInfo = new MessageInfoDto(-1);

//    @ExceptionHandler(TransactionException .class)
//    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
//    public MessageInfoDto handleTransactionException(TransactionException transactionException) {
//        messageInfo.setRespCode(Constants.INTERNAL_SERVER_ERROR);
//        messageInfo.setMessage(transactionException.getMessage());
//        return messageInfo;
//    }

    @ExceptionHandler(PaymentNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public MessageInfoDto handlePaymentNotFound(PaymentNotFoundException paymentNotFound) {
        log.info("Input handlePaymentNotFound. {}", paymentNotFound.getMessage());

        messageInfo.setRespCode(Constants.NOT_FOUND);
        messageInfo.setMessage(paymentNotFound.getMessage());

        log.error("Output handlePaymentNotFound. messageInfo={ errorCode={}, respCode={}, message={} }",
                messageInfo.getErrorCode(), messageInfo.getRespCode(), messageInfo.getMessage());
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