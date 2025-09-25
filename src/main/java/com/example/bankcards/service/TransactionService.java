package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;

import java.util.List;


public interface TransactionService {
    
    TransactionResponse transfer(TransactionRequest request);
    List<TransactionResponse> getCardTransactions(String username, Long cardId);
}