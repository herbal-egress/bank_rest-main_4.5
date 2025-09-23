package com.example.bankcards.dto.transaction;

// добавленный код: Импорт Lombok и времени
import lombok.Data;

import java.time.LocalDateTime;

// добавленный код: DTO для ответа о транзакции
@Data
public class TransactionResponse {

    // добавленный код: ID транзакции
    private Long id;

    // добавленный код: ID карты-отправителя
    private Long fromCardId;

    // добавленный код: ID карты-получателя
    private Long toCardId;

    // добавленный код: Сумма
    private Double amount;

    // добавленный код: Время транзакции
    private LocalDateTime timestamp;

    // добавленный код: Статус
    private String status;
}