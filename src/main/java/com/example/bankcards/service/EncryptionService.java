// service/EncryptionService.java
package com.example.bankcards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Добавленный код: Сервис для шифрования/дешифрования данных.
 * В реальном приложении здесь была бы интеграция с pgcrypto.
 */
@Service
@Slf4j
public class EncryptionService {

    /**
     * Добавленный код: Шифрует данные (заглушка).
     * В реальности: SELECT pgp_sym_encrypt('card_number', 'encryption_key')
     */
    public String encrypt(String data) {
        log.debug("Шифрование данных: {}", data.substring(0, Math.min(4, data.length())) + "***");
        // Заглушка - в реальном приложении здесь было бы настоящее шифрование
        return "encrypted_" + data;
    }

    /**
     * Добавленный код: Дешифрует данные (заглушка).
     * В реальности: SELECT pgp_sym_decrypt('encrypted_data', 'encryption_key')
     */
    public String decrypt(String encryptedData) {
        log.debug("Дешифрование данных");
        // Заглушка - в реальном приложении здесь было бы настоящее дешифрование
        if (encryptedData.startsWith("encrypted_")) {
            return encryptedData.substring("encrypted_".length());
        }
        return encryptedData;
    }
}