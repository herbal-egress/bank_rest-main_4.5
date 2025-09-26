package com.example.bankcards.service;

import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.SameCardTransferException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TransactionServiceTest {
    @Autowired
    private TransactionService transactionService;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private TransactionRepository transactionRepository;
    @MockBean
    private UserRepository userRepository;
    private TransactionRequest request;
    private Card fromCard;
    private Card toCard;
    private User fromUser;
    private User toUser;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        fromUser = new User();
        fromUser.setId(1L);
        fromUser.setUsername("testuser");
        toUser = new User();
        toUser.setId(2L);
        toUser.setUsername("otheruser");
        request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(4L);
        request.setAmount(100.0);
        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(1000.0);
        fromCard.setUser(fromUser);
        toCard = new Card();
        toCard.setId(4L);
        toCard.setBalance(5000.0);
        toCard.setUser(toUser);
        mockTransaction = new Transaction();
        mockTransaction.setId(1L);
        mockTransaction.setAmount(100.0);
        mockTransaction.setStatus(Transaction.Status.SUCCESS);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(4L)).thenReturn(Optional.of(toCard));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(fromUser));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
    }

    @Test
    @WithMockUser(username = "user")
    void transfer_Success() {
        fromCard.setStatus(Card.Status.ACTIVE);
        toCard.setStatus(Card.Status.ACTIVE);
        mockTransaction.setFromCard(fromCard);
        mockTransaction.setToCard(toCard);
        TransactionResponse response = transactionService.transfer(request);
        assertNotNull(response.getId());
        assertEquals(100.0, response.getAmount());
        assertEquals(Transaction.Status.SUCCESS.name(), response.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(4L);
        verify(userRepository, times(1)).findByUsername("user");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(username = "user")
    void transfer_FromCardNotFound_ThrowsException() {
        request.setFromCardId(999L);
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(1)).findById(999L);
        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void transfer_SameCard_ThrowsException() {
        request.setToCardId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        assertThrows(SameCardTransferException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(2)).findById(1L);
        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void transfer_InsufficientFunds_ThrowsException() {
        request.setAmount(10000.0);
        fromCard.setStatus(Card.Status.ACTIVE);
        toCard.setStatus(Card.Status.ACTIVE);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(fromUser));
        assertThrows(InsufficientFundsException.class, () -> transactionService.transfer(request));
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).findById(4L);
        verify(userRepository, times(1)).findByUsername("user");
    }
}