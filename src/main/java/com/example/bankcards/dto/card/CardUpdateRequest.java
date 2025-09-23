package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.YearMonth;

/**
 * добавленный код: DTO для запроса на обновление карты.
 * изменил ИИ: Удалено поле cardNumber, так как номер карты теперь генерируется автоматически и не редактируется.
 */
@Schema(description = "Запрос на обновление банковской карты")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // добавленный код: Игнорирует null поля
public class CardUpdateRequest {

    @Schema(description = "Новое имя владельца карты (верхний регистр, макс. 50 символов). Опционально для обновления",
            example = "JOHN DOE")
    @Size(max = 50, message = "Имя владельца не должно превышать 50 символов")
    private String ownerName;

    @Schema(description = "Новый срок действия карты в формате YYYY-MM. Опционально для обновления",
            example = "2027-12")
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth expirationDate;
}