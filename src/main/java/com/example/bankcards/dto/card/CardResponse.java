// dto/card/CardResponse.java
package com.example.bankcards.dto.card;

import com.example.bankcards.entity.Card;
import lombok.Data;

import java.time.YearMonth;

/**
 * Добавленный код: DTO для ответа с данными карты.
 * Содержит маскированный номер карты.
 */
@Data
public class CardResponse {
    private Long id;
    private String maskedCardNumber; // Маскированный номер
    private String ownerName;
    private YearMonth expirationDate;
    private Card.Status status;
    private Double balance;
    private Long userId;

    /**
     * Добавленный код: Маскирует номер карты (**** **** **** 1234)
     */
    public static String maskCardNumber(String encryptedCardNumber) {
        // В реальном приложении здесь было бы расшифрование
        // Для примера просто маскируем последние 4 цифры
        if (encryptedCardNumber == null || encryptedCardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        String lastFour = encryptedCardNumber.substring(encryptedCardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}