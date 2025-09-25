package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static com.example.bankcards.entity.Card.Status.ACTIVE;
import static com.example.bankcards.entity.Card.Status.BLOCKED;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void getUserCards_ShouldReturnUserCards() throws Exception {

        CardResponse card1 = new CardResponse();
        card1.setId(1L);
        card1.setMaskedCardNumber("4111113457681111");
        card1.setOwnerName("User One");
        card1.setExpirationDate(YearMonth.of(2025, 12));
        card1.setBalance(1000.0);
        card1.setStatus(ACTIVE);

        CardResponse card2 = new CardResponse();
        card2.setId(2L);
        card2.setMaskedCardNumber("4222224567832222");
        card2.setOwnerName("User One");
        card2.setExpirationDate(YearMonth.of(2026, 6));
        card2.setBalance(500.0);
        card2.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card1, card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());

        // Исправлено: используем eq() для userId и any() для pageable
        when(cardService.getUserCards(eq(1L), any(Pageable.class))).thenReturn(pageCards);


        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("************1111"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.0))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].maskedCardNumber").value("************2222"))
                .andExpect(jsonPath("$.content[1].balance").value(500.0));


        verify(cardService, times(1)).getUserCards(eq(1L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_ShouldReturnCard() throws Exception {

        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("4111113456721111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(1000.0);
        card.setStatus(ACTIVE);

        when(cardService.getCardById(1L)).thenReturn(card);


        mockMvc.perform(get("/api/user/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("************1111"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));


        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_WithBlockedStatus_ShouldReturnBlockedCard() throws Exception {


        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("4111111111111111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(0.0);
        card.setStatus(BLOCKED);

        when(cardService.getCardById(1L)).thenReturn(card);


        mockMvc.perform(get("/api/user/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("************1111"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.status").value("BLOCKED"));


        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user2")
    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {

        CardResponse card = new CardResponse();
        card.setId(3L);
        card.setMaskedCardNumber("4333333456783333");
        card.setOwnerName("User Two");
        card.setExpirationDate(YearMonth.of(2025, 9));
        card.setBalance(750.0);
        card.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());

        // Исправлено: используем eq() для userId и any() для pageable
        when(cardService.getUserCards(eq(2L), any(Pageable.class))).thenReturn(pageCards);


        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(3L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("************3333"))
                .andExpect(jsonPath("$.content[0].balance").value(750.0));


        verify(cardService, times(1)).getUserCards(eq(2L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardTransactions_ShouldReturnTransactions() throws Exception {

        LocalDateTime timestamp1 = LocalDateTime.parse("2024-01-01T10:00:00");
        LocalDateTime timestamp2 = LocalDateTime.parse("2024-01-02T11:00:00");

        TransactionResponse transaction1 = new TransactionResponse();
        transaction1.setId(1L);
        transaction1.setFromCardId(1L);
        transaction1.setToCardId(2L);
        transaction1.setAmount(100.0);
        transaction1.setTimestamp(timestamp1);

        TransactionResponse transaction2 = new TransactionResponse();
        transaction2.setId(2L);
        transaction2.setFromCardId(1L);
        transaction2.setToCardId(3L);
        transaction2.setAmount(50.0);
        transaction2.setTimestamp(timestamp2);

        List<TransactionResponse> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getCardTransactions("user1", 1L)).thenReturn(transactions);


        mockMvc.perform(get("/api/user/cards/{cardId}/transactions", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].timestamp").value("2024-01-01T10:00:00"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(50.0))
                .andExpect(jsonPath("$[1].timestamp").value("2024-01-02T11:00:00"));


        verify(transactionService, times(1)).getCardTransactions("user1", 1L);
        verifyNoMoreInteractions(transactionService);
    }
}