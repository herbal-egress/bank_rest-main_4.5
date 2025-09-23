package com.example.bankcards.exception;

// добавленный код: Кастомное исключение для недостатка средств
public class InsufficientFundsException extends RuntimeException {
    // добавленный код: Конструктор с сообщением на русском
    public InsufficientFundsException(String message) {
        super(message);
    }
}