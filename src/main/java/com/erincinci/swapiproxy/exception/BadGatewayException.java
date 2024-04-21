package com.erincinci.swapiproxy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_GATEWAY, reason = "External API failure")
public class BadGatewayException extends RuntimeException {

    public BadGatewayException(Throwable cause) {
        super(cause);
    }
}
