package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest; // добавленный код: импортируем TransactionRequest для теста transfer
import com.example.bankcards.dto.transaction.TransactionResponse; // добавленный код: импортируем TransactionResponse для теста transfer
import com.example.bankcards.entity.User; // добавленный код: импортируем User для мока в userRepository
import com.example.bankcards.repository.UserRepository; // добавленный код: импортируем UserRepository для @MockBean
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

import java.time.LocalDateTime; // добавленный код: импортируем LocalDateTime для timestamp в TransactionResponse
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet; // добавленный код: импортируем HashSet для пустых ролей в User
import java.util.List;
import java.util.Optional; // добавленный код: импортируем Optional для мока findByUsername

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

    @MockBean // добавленный код: добавляем @MockBean для UserRepository, чтобы мокать findByUsername и избежать UserNotFoundException из реальной БД
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void getUserCards_ShouldReturnUserCards() throws Exception {

        // добавленный код: мок для userRepository.findByUsername, возвращаем User с id=1L для user1 (пустые роли, так как не проверяем)
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
        // добавленный код: проверяем вызов userRepository.findByUsername
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_ShouldReturnCard() throws Exception {

        // добавленный код: мок для userRepository.findByUsername, возвращаем User с id=1L для user1
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
        // добавленный код: проверяем вызов userRepository.findByUsername
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void getCardById_WithBlockedStatus_ShouldReturnBlockedCard() throws Exception {

        // добавленный код: мок для userRepository.findByUsername, возвращаем User с id=1L для user1
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
        // добавленный код: проверяем вызов userRepository.findByUsername
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user2")
    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {

        // добавленный код: мок для userRepository.findByUsername, возвращаем User с id=2L для user2
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
        // добавленный код: проверяем вызов userRepository.findByUsername
        verify(userRepository, times(1)).findByUsername("user2");
    }

    @Test
    @WithMockUser(username = "user1")
    void blockUserCard_ShouldBlockCard() throws Exception {
        // добавленный код: тест для эндпоинта POST /api/user/cards/{id}/block (существующий в контроллере), вызывает cardService.blockCard(id), возвращает CardResponse с status=BLOCKED

        // добавленный код: мок для userRepository.findByUsername
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));

        CardResponse blockedCard = new CardResponse();
        blockedCard.setId(1L);
        blockedCard.setMaskedCardNumber("**** **** **** 1111");
        blockedCard.setOwnerName("User One"); // добавленный код: установка ownerName для полной проверки DTO в ответе
        blockedCard.setExpirationDate(YearMonth.of(2025, 12)); // добавленный код: установка expirationDate для полной проверки DTO в ответе
        blockedCard.setBalance(1000.0);
        blockedCard.setStatus(BLOCKED);

        // добавленный код: мок для cardService.blockCard, возвращаем blockedCard
        when(cardService.blockCard(1L)).thenReturn(blockedCard);

        mockMvc.perform(post("/api/user/cards/{id}/block", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$.ownerName").value("User One")) // добавленный код: проверка ownerName в ответе для полноты теста
                .andExpect(jsonPath("$.expirationDate").value("2025-12")) // добавленный код: проверка expirationDate в ответе (формат yyyy-MM от @JsonFormat)
                .andExpect(jsonPath("$.balance").value(1000.0));

        // добавленный код: проверяем вызов cardService.blockCard
        verify(cardService, times(1)).blockCard(1L);
        verifyNoMoreInteractions(cardService);
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    @WithMockUser(username = "user1")
    void transfer_ShouldPerformTransfer() throws Exception {
        // добавленный код: тест для эндпоинта POST /api/user/transactions/transfer (существующий в контроллере), вызывает transactionService.transfer(request), возвращает TransactionResponse с status 201

        // добавленный код: мок для userRepository.findByUsername
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>())));

        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(100.0);

        LocalDateTime timestamp = LocalDateTime.parse("2024-01-01T10:00:00"); // добавленный код: фиксированный timestamp для воспроизводимости теста и проверки jsonPath

        TransactionResponse response = new TransactionResponse();
        response.setId(1L);
        response.setFromCardId(1L);
        response.setToCardId(2L);
        response.setAmount(100.0);
        response.setTimestamp(timestamp); // изменил ИИ: установка фиксированного timestamp для проверки в jsonPath (избегание несоответствия от LocalDateTime.now())
        response.setStatus("SUCCESS");

        // добавленный код: мок для transactionService.transfer, возвращаем response
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
                .andExpect(jsonPath("$.timestamp").value("2024-01-01T10:00:00")) // добавленный код: проверка фиксированного timestamp в ответе для полноты теста
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // добавленный код: проверяем вызов transactionService.transfer с request
        verify(transactionService, times(1)).transfer(any(TransactionRequest.class));
        verifyNoMoreInteractions(transactionService);
        verify(userRepository, times(1)).findByUsername("user1");
    }
}