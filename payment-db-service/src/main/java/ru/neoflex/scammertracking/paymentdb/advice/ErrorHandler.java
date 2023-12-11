package ru.neoflex.scammertracking.paymentdb.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import ru.neoflex.scammertracking.paymentdb.domain.dto.MessageInfoDto;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentAlreadyExistsException;
import ru.neoflex.scammertracking.paymentdb.error.exception.PaymentNotFoundException;
import ru.neoflex.scammertracking.paymentdb.error.validation.Violation;
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
//        return Mono.error(new PaymentNotFoundException("err"));
    }

    @ExceptionHandler(PaymentAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<MessageInfoDto>> handlePaymentNotFound(PaymentAlreadyExistsException resourceExists) {
        messageInfo.setErrorCode(-2);
        messageInfo.setRespCode(Constants.BAD_REQUEST);
        messageInfo.setMessage(resourceExists.getMessage());
        ResponseEntity<MessageInfoDto> responseEntity = ResponseEntity
                .badRequest()
                .header(Constants.CORRELATION_ID_HEADER_NAME, resourceExists.getCorrelationId().toString())
                .body(messageInfo);
        return Mono.just(responseEntity);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Mono<List<Violation>> handleException(WebExchangeBindException e) {
        List<Violation> violations = e
                .getFieldErrors()
                .stream()
                .map(val -> new Violation(val.getField(), val.getDefaultMessage()))
                .toList();
        return Mono.just(violations);
    }
}