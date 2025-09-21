// exception/InvalidRoleException.java (Новый кастомный exception)
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Исключение для неверной роли.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String message) {
        super(message);
    }
    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}