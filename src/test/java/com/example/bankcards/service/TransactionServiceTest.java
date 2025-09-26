package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.SameCardTransferException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// добавленный код: Удалены зависимости от Testcontainers и PostgreSQL
@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    private TransactionRequest request;
    private Card fromCard;
    private Card toCard;

    // добавленный код: Настройка моков и тестовых данных
    @BeforeEach
    void setUp() {
        request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(4L);
        request.setAmount(100.0);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(1000.0);

        toCard = new Card();
        toCard.setId(4L);
        toCard.setBalance(5000.0);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(4L)).thenReturn(Optional.of(toCard));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
    }

    // добавленный код: Тест успешного перевода
    @Test
    @WithMockUser(username = "user")
    void transfer_Success() {
        TransactionResponse response = transactionService.transfer(request);
        assertNotNull(response.getId());
        assertEquals(100.0, response.getAmount());
        assertEquals(Transaction.Status.SUCCESS.name(), response.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(4L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    // добавленный код: Тест перевода с несуществующей карты
    @Test
    @WithMockUser(username = "user")
    void transfer_FromCardNotFound_ThrowsException() {
        request.setFromCardId(999L);
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(1)).findById(999L);
    }

    // добавленный код: Тест перевода на ту же карту
    @Test
    @WithMockUser(username = "user")
    void transfer_SameCard_ThrowsException() {
        request.setToCardId(1L);
        assertThrows(SameCardTransferException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(1)).findById(1L);
    }

    // добавленный код: Тест перевода с недостаточным балансом
    @Test
    @WithMockUser(username = "user")
    void transfer_InsufficientFunds_ThrowsException() {
        request.setAmount(10000.0);
        assertThrows(InsufficientFundsException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(1)).findById(1L);
    }
}