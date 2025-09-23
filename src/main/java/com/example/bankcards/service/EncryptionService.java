package com.example.bankcards.service;

/**
 * добавленный код: Интерфейс для сервиса шифрования/дешифрования данных.
 */
public interface EncryptionService {
    /**
     * добавленный код: Шифрует данные.
     * @param data Данные для шифрования
     * @return Зашифрованные данные
     */
    String encrypt(String data);

    /**
     * добавленный код: Дешифрует данные.
     * @param encryptedData Зашифрованные данные
     * @return Расшифрованные данные
     */
    String decrypt(String encryptedData);
}