package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;

import java.util.List;

/**
 * добавленный код: Интерфейс для сервиса управления транзакциями.
 */
public interface TransactionService {
    /**
     * добавленный код: Выполняет перевод между картами.
     * @param request DTO с данными о переводе (fromCardId, toCardId, amount)
     * @return TransactionResponse с данными о выполненной транзакции
     */
    TransactionResponse transfer(TransactionRequest request);
    List<TransactionResponse> getCardTransactions(String username, Long cardId);
}