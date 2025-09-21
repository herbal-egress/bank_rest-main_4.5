// dto/card/CardRequest.java - ИСПРАВЛЕНИЕ SWAGGER СХЕМЫ
package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
// изменил ИИ: Добавлен импорт для @Schema
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * Добавленный код: DTO для запроса на создание или обновление карты.
 * изменил ИИ: Добавлена документация для Swagger
 */
@Schema(description = "Запрос на создание банковской карты")
@Data
public class CardRequest {

    @Schema(description = "Номер карты (16 цифр, будет зашифрован)",
            example = "4532015112830366",
            required = true)
    @NotBlank(message = "Номер карты не может быть пустым")
    private String cardNumber; // Будет зашифрован при сохранении

    @Schema(description = "Имя владельца карты (верхний регистр, макс. 50 символов)",
            example = "JOHN DOE",
            required = true)
    @NotBlank(message = "Имя владельца не может быть пустым")
    @Size(max = 50, message = "Имя владельца не должно превышать 50 символов")
    private String ownerName;

    @Schema(description = "Срок действия карты в формате YYYY-MM",
            example = "2027-12",
            required = true)
    @NotNull(message = "Срок действия не может быть пустым")
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth expirationDate;

    @Schema(description = "Начальный баланс карты",
            example = "1000.50",
            defaultValue = "0.0")
    private Double balance = 0.0;

    @Schema(description = "ID пользователя-владельца карты",
            example = "2",
            required = true)
    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;
}