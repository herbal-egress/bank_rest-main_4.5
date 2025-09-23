package com.example.bankcards.exception;

// добавленный код: Кастомное исключение для перевода на ту же карту
public class SameCardTransferException extends RuntimeException {
    // добавленный код: Конструктор с сообщением на русском
    public SameCardTransferException(String message) {
        super(message);
    }
}