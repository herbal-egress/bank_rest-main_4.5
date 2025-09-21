// dto/card/CardRequest.java
package com.example.bankcards.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * Добавленный код: DTO для запроса на создание или обновление карты.
 */
@Data
public class CardRequest {

    @NotBlank(message = "Номер карты не может быть пустым")
    private String cardNumber; // Будет зашифрован при сохранении

    @NotBlank(message = "Имя владельца не может быть пустым")
    @Size(max = 50, message = "Имя владельца не должно превышать 50 символов")
    private String ownerName;

    @NotNull(message = "Срок действия не может быть пустым")
    private YearMonth expirationDate;

    private Double balance = 0.0;

    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;
}