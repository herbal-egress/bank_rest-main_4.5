// exception/UserNotFoundException.java
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

/**
 * Добавленный код: Кастомное исключение для случаев, когда пользователь не найден.
 * Возвращает HTTP 404 Not Found.
 */
@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    // Добавленный код: Конструктор с сообщением об ошибке.
    public UserNotFoundException(String message) {
        super(message);
    }

    // Добавленный код: Конструктор с сообщением и причиной.
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}