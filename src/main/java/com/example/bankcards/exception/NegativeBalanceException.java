// exception/NegativeBalanceException.java (Новый кастомный exception)
package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Исключение для отрицательного баланса.
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NegativeBalanceException extends RuntimeException {
    public NegativeBalanceException(String message) {
        super(message);
    }
    public NegativeBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}