
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;


@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {

    
    public InvalidTokenException(String message) {
        super(message);
    }

    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}