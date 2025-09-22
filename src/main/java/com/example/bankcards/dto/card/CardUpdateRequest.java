// dto/card/CardUpdateRequest.java - НОВЫЙ DTO ДЛЯ ОБНОВЛЕНИЯ КАРТЫ
package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * Добавленный код: DTO для запроса на обновление карты.
 * Обновляет только номер карты, имя владельца и срок действия.
 */
@Schema(description = "Запрос на обновление банковской карты")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // Игнорирует null поля
public class CardUpdateRequest {

    @Schema(description = "Новый номер карты (16 цифр, будет зашифрован). Опционально для обновления",
            example = "4532015112830366")
    private String cardNumber;

    @Schema(description = "Новое имя владельца карты (верхний регистр, макс. 50 символов). Опционально для обновления",
            example = "JOHN DOE")
    @Size(max = 50, message = "Имя владельца не должно превышать 50 символов")
    private String ownerName;

    @Schema(description = "Новый срок действия карты в формате YYYY-MM. Опционально для обновления",
            example = "2027-12")
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth expirationDate;
}