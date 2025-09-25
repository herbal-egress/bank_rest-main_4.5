package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
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
import org.springframework.data.domain.PageRequest; // добавленный код: Импорт для создания Pageable в тестах
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
        // Arrange
        CardResponse card1 = new CardResponse();
        card1.setId(1L);
        card1.setMaskedCardNumber("411111******1111");
        card1.setOwnerName("User One");
        card1.setExpirationDate(YearMonth.of(2025, 12));
        card1.setBalance(1000.00);
        card1.setStatus(ACTIVE);

        CardResponse card2 = new CardResponse();
        card2.setId(2L);
        card2.setMaskedCardNumber("422222******2222");
        card2.setOwnerName("User One");
        card2.setExpirationDate(YearMonth.of(2026, 6));
        card2.setBalance(500.00);
        card2.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card1, card2);
        Pageable pageable = PageRequest.of(0, 10); // добавленный код: Создание Pageable для мокирования (страница 0, размер 10 по умолчанию)
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size()); // добавленный код: PageImpl с pageable для полной совместимости

        when(cardService.getUserCards(1L, any(Pageable.class))).thenReturn(pageCards); // изменил ИИ: Исправлено на Long userId (1L для user1) и any(Pageable.class); метод сервиса ожидает Long, Pageable

        // Act & Assert
        mockMvc.perform(get("/api/user/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("411111******1111")) // изменил ИИ: Маскированный номер для безопасности в DTO
                .andExpect(jsonPath("$[0].balance").value(1000.00))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].maskedCardNumber").value("422222******2222"))
                .andExpect(jsonPath("$[1].balance").value(500.00));

        // Verify
        verify(cardService, times(1)).getUserCards(1L, any(Pageable.class)); // изменил ИИ: Верификация с Long userId и any(Pageable.class)
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_ShouldReturnCard() throws Exception {
        // Arrange
        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("411111******1111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(1000.00);
        card.setStatus(ACTIVE);

        when(cardService.getCardById(1L)).thenReturn(card); // изменил ИИ: Метод getCardById принимает только Long id (проверка владельца в контроллере)

        // Act & Assert
        mockMvc.perform(get("/api/user/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("411111******1111"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // Verify
        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);
    }

       @Test
    @WithMockUser(username = "user2")
    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {
        // Arrange
        CardResponse card = new CardResponse();
        card.setId(3L);
        card.setMaskedCardNumber("433333******3333");
        card.setOwnerName("User Two");
        card.setExpirationDate(YearMonth.of(2025, 9));
        card.setBalance(750.00);
        card.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card);
        Pageable pageable = PageRequest.of(0, 10); // добавленный код: Pageable для мокирования
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());

        when(cardService.getUserCards(2L, any(Pageable.class))).thenReturn(pageCards); // изменил ИИ: Long userId (2L для user2), any(Pageable.class)

        // Act & Assert
        mockMvc.perform(get("/api/user/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(3L))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("433333******3333")) // изменил ИИ: maskedCardNumber вместо cardNumber
                .andExpect(jsonPath("$[0].balance").value(750.00));

        // Verify
        verify(cardService, times(1)).getUserCards(2L, any(Pageable.class)); // изменил ИИ: Long userId и any(Pageable.class)
        verifyNoMoreInteractions(cardService);
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardTransactions_ShouldReturnTransactions() throws Exception {
        // Arrange
        LocalDateTime timestamp1 = LocalDateTime.parse("2024-01-01T10:00:00"); // добавленный код: Конкретные значения для стабильности
        LocalDateTime timestamp2 = LocalDateTime.parse("2024-01-02T11:00:00");

        TransactionResponse transaction1 = new TransactionResponse();
        transaction1.setId(1L);
        transaction1.setFromCardId(1L);
        transaction1.setToCardId(2L);
        transaction1.setAmount(100.00);
        transaction1.setTimestamp(timestamp1);

        TransactionResponse transaction2 = new TransactionResponse();
        transaction2.setId(2L);
        transaction2.setFromCardId(1L);
        transaction2.setToCardId(3L);
        transaction2.setAmount(50.00);
        transaction2.setTimestamp(timestamp2);

        List<TransactionResponse> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionService.getCardTransactions("user1", 1L)).thenReturn(transactions); // изменил ИИ: Сигнатура метода: String username, Long cardId (контроллер передаёт username из SecurityContext)

        // Act & Assert
        mockMvc.perform(get("/api/user/cards/{id}/transactions", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].timestamp").value("2024-01-01T10:00:00"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(50.00))
                .andExpect(jsonPath("$[1].timestamp").value("2024-01-02T11:00:00"));

        // Verify
        verify(transactionService, times(1)).getCardTransactions("user1", 1L);
        verifyNoMoreInteractions(transactionService);
    }
}