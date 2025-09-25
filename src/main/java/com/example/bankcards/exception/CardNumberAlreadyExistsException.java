
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
public class CardNumberAlreadyExistsException extends RuntimeException {
    public CardNumberAlreadyExistsException(String message) {
        super(message);
    }
    public CardNumberAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}