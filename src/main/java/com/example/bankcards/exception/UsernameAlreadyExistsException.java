// exception/UsernameAlreadyExistsException.java
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Добавленный код: Исключение, выбрасываемое при попытке создать пользователя с уже существующим именем.
 */
@ResponseStatus(HttpStatus.CONFLICT) // HTTP 409 Conflict
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
    public UsernameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}