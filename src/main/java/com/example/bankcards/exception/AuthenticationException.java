// exception/AuthenticationException.java
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

/**
 * Добавленный код: Кастомное исключение для ошибок аутентификации.
 * Возвращает HTTP 401 Unauthorized.
 */
@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {

    // Добавленный код: Конструктор с сообщением об ошибке.
    public AuthenticationException(String message) {
        super(message);
    }

    // Добавленный код: Конструктор с сообщением и причиной.
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}