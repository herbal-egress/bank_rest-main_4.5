//// UserCardControllerTest.java
//package com.example.bankcards.controller;
//
//import com.example.bankcards.dto.card.CardResponse;
//import com.example.bankcards.dto.transaction.TransactionRequest;
//import com.example.bankcards.dto.transaction.TransactionResponse;
//import com.example.bankcards.service.CardService;
//import com.example.bankcards.service.TransactionService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Page;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.YearMonth;
//import java.util.Arrays;
//import java.util.List;
//
//import static com.example.bankcards.entity.Card.Status.ACTIVE;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
//@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
//class UserCardControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private CardService cardService;
//
//    @MockBean
//    private TransactionService transactionService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @WithMockUser(username = "user1")
//    void getUserCards_ShouldReturnUserCards() throws Exception {
//        // Arrange
//        CardResponse card1 = new CardResponse();
//        card1.setId(1L);
//        card1.setMaskedCardNumber("4111111111111111");
//        card1.setOwnerName("User One");
//        card1.setExpirationDate(YearMonth.of(2025, 12));
//        card1.setBalance(1000.00);
//        card1.setStatus(ACTIVE);
//
//        CardResponse card2 = new CardResponse();
//        card2.setId(2L);
//        card2.setMaskedCardNumber("4222222222222222");
//        card2.setOwnerName("User One");
//        card2.setExpirationDate(YearMonth.of(2026, 6));
//        card2.setBalance(500.00);
//        card2.setStatus(ACTIVE);
//
//        List<CardResponse> cards = Arrays.asList(card1, card2);
//
//        when(cardService.getUserCards(1L, 1)).thenReturn((Page<CardResponse>) cards);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/user/cards")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].id").value(1L))
//                .andExpect(jsonPath("$[0].cardNumber").value("4111111111111111"))
//                .andExpect(jsonPath("$[0].balance").value(1000.00))
//                .andExpect(jsonPath("$[1].id").value(2L))
//                .andExpect(jsonPath("$[1].cardNumber").value("4222222222222222"))
//                .andExpect(jsonPath("$[1].balance").value(500.00));
//
//        // Verify
//        verify(cardService, times(1)).getUserCards("user1");
//        verifyNoMoreInteractions(cardService);
//    }
//
//    @Test
//    @WithMockUser(username = "user1")
//    void getCardById_ShouldReturnCard() throws Exception {
//        // Arrange
//        CardResponse card = new CardResponse();
//        card.setId(1L);
//        card.setMaskedCardNumber("4111111111111111");
//        card.setOwnerName("User One");
//        card.setExpirationDate(YearMonth.of(2025, 12));
//        card.setBalance(1000.00);
//        card.setStatus(ACTIVE);
//
//        when(cardService.getUserCardById("user1", 1L)).thenReturn(card);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/user/cards/{id}", 1L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.cardNumber").value("4111111111111111"))
//                .andExpect(jsonPath("$.balance").value(1000.00))
//                .andExpect(jsonPath("$.status").value("ACTIVE"));
//
//        // Verify
//        verify(cardService, times(1)).getUserCardById("user1", 1L);
//        verifyNoMoreInteractions(cardService);
//    }
//
//    @Test
//    @WithMockUser(username = "user1")
//    void blockCard_ShouldBlockCard() throws Exception {
//        // Arrange
//        CardResponse blockedCard = new CardResponse();
//        blockedCard.setId(1L);
//        blockedCard.setMaskedCardNumber("4111111111111111");
//        blockedCard.setOwnerName("User One");
//        blockedCard.setExpirationDate(YearMonth.of(2025, 12));
//        blockedCard.setCvv("123");
//        blockedCard.setBalance(BigDecimal.valueOf(1000.00));
//        blockedCard.setStatus("BLOCKED");
//
//        when(cardService.blockCard("user1", 1L)).thenReturn(blockedCard);
//
//        // Act & Assert
//        mockMvc.perform(put("/api/user/cards/{id}/block", 1L)
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.status").value("BLOCKED"));
//
//        // Verify
//        verify(cardService, times(1)).blockCard("user1", 1L);
//        verifyNoMoreInteractions(cardService);
//    }
//
//    @Test
//    @WithMockUser(username = "user1")
//    void transfer_ShouldPerformTransfer() throws Exception {
//        // Arrange
//        TransactionRequest transactionRequest = new TransactionRequest();
//        transactionRequest.setFromCardId(1L);
//        transactionRequest.setToCardId(2L);
//        transactionRequest.setAmount(BigDecimal.valueOf(100.00));
//
//        TransactionResponse transactionResponse = new TransactionResponse();
//        transactionResponse.setId(1L);
//        transactionResponse.setFromCardId(1L);
//        transactionResponse.setToCardId(2L);
//        transactionResponse.setAmount(BigDecimal.valueOf(100.00));
//        transactionResponse.setTimestamp("2024-01-01T10:00:00");
//
//        when(transactionService.transfer(anyString(), any(TransactionRequest.class))).thenReturn(transactionResponse);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/user/transfer")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(transactionRequest)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.fromCardId").value(1L))
//                .andExpect(jsonPath("$.toCardId").value(2L))
//                .andExpect(jsonPath("$.amount").value(100.00))
//                .andExpect(jsonPath("$.timestamp").value("2024-01-01T10:00:00"));
//
//        // Verify
//        verify(transactionService, times(1)).transfer(eq("user1"), any(TransactionRequest.class));
//        verifyNoMoreInteractions(transactionService);
//    }
//
//    @Test
//    @WithMockUser(username = "user2")
//    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {
//        // Arrange
//        CardResponse card = new CardResponse();
//        card.setId(3L);
//        card.setMaskedCardNumber("4333333333333333");
//        card.setOwnerName("User Two");
//        card.setExpirationDate(YearMonth.of(2025, 9));
//        card.setCvv("789");
//        card.setBalance(BigDecimal.valueOf(750.00));
//        card.setStatus("ACTIVE");
//
//        List<CardResponse> cards = Arrays.asList(card);
//
//        when(cardService.getUserCards("user2")).thenReturn(cards);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/user/cards")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].id").value(3L))
//                .andExpect(jsonPath("$[0].cardNumber").value("4333333333333333"))
//                .andExpect(jsonPath("$[0].balance").value(750.00));
//
//        // Verify
//        verify(cardService, times(1)).getUserCards("user2");
//        verifyNoMoreInteractions(cardService);
//    }
//
//    @Test
//    @WithMockUser(username = "user1")
//    void getCardTransactions_ShouldReturnTransactions() throws Exception {
//        // Arrange
//        TransactionResponse transaction1 = new TransactionResponse();
//        transaction1.setId(1L);
//        transaction1.setFromCardId(1L);
//        transaction1.setToCardId(2L);
//        transaction1.setAmount(BigDecimal.valueOf(100.00));
//        transaction1.setTimestamp("2024-01-01T10:00:00");
//
//        TransactionResponse transaction2 = new TransactionResponse();
//        transaction2.setId(2L);
//        transaction2.setFromCardId(1L);
//        transaction2.setToCardId(3L);
//        transaction2.setAmount(BigDecimal.valueOf(50.00));
//        transaction2.setTimestamp("2024-01-02T11:00:00");
//
//        List<TransactionResponse> transactions = Arrays.asList(transaction1, transaction2);
//
//        when(transactionService.getCardTransactions("user1", 1L)).thenReturn(transactions);
//
//        // Act & Assert
//        mockMvc.perform(get("/api/user/cards/{id}/transactions", 1L)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$[0].id").value(1L))
//                .andExpect(jsonPath("$[0].amount").value(100.00))
//                .andExpect(jsonPath("$[1].id").value(2L))
//                .andExpect(jsonPath("$[1].amount").value(50.00));
//
//        // Verify
//        verify(transactionService, times(1)).getCardTransactions("user1", 1L);
//        verifyNoMoreInteractions(transactionService);
//    }
//}