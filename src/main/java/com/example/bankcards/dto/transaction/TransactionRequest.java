package com.example.bankcards.dto.transaction;

// добавленный код: Импорт Lombok и валидации
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// добавленный код: DTO для запроса на перевод
@Data
public class TransactionRequest {

    // добавленный код: ID карты-отправителя
    @NotNull(message = "ID карты-отправителя обязателен")
    private Long fromCardId;

    // добавленный код: ID карты-получателя
    @NotNull(message = "ID карты-получателя обязателен")
    private Long toCardId;

    // добавленный код: Сумма перевода (минимум 0.01)
    @NotNull(message = "Сумма обязательна")
    @Min(value = 1, message = "Сумма должна быть не менее 0.01")
    private Double amount;
}