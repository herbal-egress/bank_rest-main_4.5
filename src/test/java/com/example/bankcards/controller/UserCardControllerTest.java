package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void getUserCards_ShouldReturnUserCards() throws Exception {


        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));

        CardResponse card1 = new CardResponse();
        card1.setId(1L);

        card1.setMaskedCardNumber("**** **** **** 1111");
        card1.setOwnerName("User One");
        card1.setExpirationDate(YearMonth.of(2025, 12));
        card1.setBalance(1000.0);
        card1.setStatus(ACTIVE);

        CardResponse card2 = new CardResponse();
        card2.setId(2L);

        card2.setMaskedCardNumber("**** **** **** 2222");
        card2.setOwnerName("User One");
        card2.setExpirationDate(YearMonth.of(2026, 6));
        card2.setBalance(500.0);
        card2.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card1, card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());


        when(cardService.getUserCards(eq(1L), any(Pageable.class))).thenReturn(pageCards);


        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1L))

                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.content[0].balance").value(1000.0))
                .andExpect(jsonPath("$.content[1].id").value(2L))

                .andExpect(jsonPath("$.content[1].maskedCardNumber").value("**** **** **** 2222"))
                .andExpect(jsonPath("$.content[1].balance").value(500.0));


        verify(cardService, times(1)).getUserCards(eq(1L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_ShouldReturnCard() throws Exception {


        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);

        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(1000.0);
        card.setStatus(ACTIVE);

        when(cardService.getCardById(1L)).thenReturn(card);


        mockMvc.perform(get("/api/user/cards/{id}/balance", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(1000.0));


        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_WithBlockedStatus_ShouldReturnBlockedCard() throws Exception {


        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);

        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(0.0);
        card.setStatus(BLOCKED);

        when(cardService.getCardById(1L)).thenReturn(card);


        mockMvc.perform(get("/api/user/cards/{id}/balance", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(0.0));


        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user2")
    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {


        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(new User(2L, "user2", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(3L);

        card.setMaskedCardNumber("**** **** **** 3333");
        card.setOwnerName("User Two");
        card.setExpirationDate(YearMonth.of(2025, 9));
        card.setBalance(750.0);
        card.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());


        when(cardService.getUserCards(eq(2L), any(Pageable.class))).thenReturn(pageCards);


        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(3L))

                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 3333"))
                .andExpect(jsonPath("$.content[0].balance").value(750.0));


        verify(cardService, times(1)).getUserCards(eq(2L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user2");
    }

    @Test
    @WithMockUser(username = "user1")
    void blockUserCard_ShouldMessage() throws Exception {

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));


        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 1111");
        cardResponse.setOwnerName("User One");


        when(cardService.getCardById(1L)).thenReturn(cardResponse);


        String expectedMessage = String.format(
                "Пользователь %s (id=%d) отправил запрос на блокировку карты номер %s (id=%d)",
                cardResponse.getOwnerName(), 1L, cardResponse.getMaskedCardNumber(), cardResponse.getId()
        );

        mockMvc.perform(post("/api/user/cards/{id}/block", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(jsonPath("$.message").value(expectedMessage));


        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void transfer_ShouldPerformTransfer() throws Exception {


        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(100.0);

        LocalDateTime timestamp = LocalDateTime.parse("2024-01-01T10:00:00");
        TransactionResponse response = new TransactionResponse();
        response.setId(1L);
        response.setFromCardId(1L);
        response.setToCardId(2L);
        response.setAmount(100.0);
        response.setTimestamp(timestamp);
        response.setStatus("SUCCESS");

        when(transactionService.transfer(any(TransactionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/user/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fromCardId").value(1L))
                .andExpect(jsonPath("$.toCardId").value(2L))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.timestamp").value("2024-01-01T10:00:00"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));


        verify(transactionService, times(1)).transfer(any(TransactionRequest.class));
        verifyNoMoreInteractions(transactionService);


    }
}