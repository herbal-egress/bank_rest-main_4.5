// exception/InvalidTokenException.java
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

/**
 * Добавленный код: Кастомное исключение для невалидных JWT токенов.
 * Возвращает HTTP 401 Unauthorized.
 */
@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {

    // Добавленный код: Конструктор с сообщением об ошибке.
    public InvalidTokenException(String message) {
        super(message);
    }

    // Добавленный код: Конструктор с сообщением и причиной.
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}