package com.example.bankcards.dto.transaction;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TransactionRequest {

    
    @NotNull(message = "ID карты-отправителя обязателен")
    private Long fromCardId;

    
    @NotNull(message = "ID карты-получателя обязателен")
    private Long toCardId;

    
    @NotNull(message = "Сумма обязательна")
    @Min(value = 1, message = "Сумма должна быть не менее 0.01")
    private Double amount;
}