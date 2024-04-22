package com.erincinci.swapiproxy.handler;

import com.erincinci.swapiproxy.exception.BadGatewayException;
import com.erincinci.swapiproxy.exception.BadRequestException;
import com.erincinci.swapiproxy.exception.InternalServerException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class ControllerExceptionHandler {

    public record ErrorMessage(HttpStatus status, String message) {}

    @ExceptionHandler(ConversionFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleConversion(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorMessage> handleBadRequest(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadGatewayException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ResponseEntity<ErrorMessage> handleBadGateway(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.BAD_GATEWAY, ex.getMessage()), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler({InternalServerException.class, InterruptedException.class, ExecutionException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handleInternalServerError(RuntimeException ex) {
        return new ResponseEntity<>(new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
