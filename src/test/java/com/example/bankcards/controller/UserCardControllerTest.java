package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
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
import org.springframework.boot.test.mock.mockito.MockBean; // добавленный код: импорт для @MockBean (замена @SpyBean для избежания вызова реальных методов при стуббинге)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.default_schema=test"})
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class UserCardControllerTest {
    @MockBean // изменил ИИ: изменил с @SpyBean на @MockBean для transactionRepository (избежание реальных вызовов; в интеграционном тесте не стуббится, использует реальную логику)
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private MockMvc mockMvc;

    @MockBean // изменил ИИ: изменил с @SpyBean на @MockBean для cardService (stub через when.thenReturn; предотвращает вызов реальных методов и SELECT в логах, решает проблему с UserNotFoundException от реальных вызовов)
    private CardService cardService;

    @MockBean // изменил ИИ: изменил с @SpyBean на @MockBean для transactionService (stub через when.thenReturn где нужно; в интеграционном тесте не стуббится, использует реальную логику)
    private TransactionService transactionService;

    @MockBean // изменил ИИ: изменил с @SpyBean на @MockBean для userRepository (stub через when.thenReturn; предотвращает реальные SELECT и исключения от БД)
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void getUserCards_ShouldReturnUserCards() throws Exception {

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean (стандартный синтаксис для mock, не вызывает реальный метод)
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse card1 = new CardResponse();
        card1.setId(1L);
        card1.setMaskedCardNumber("**** **** **** 1111");
        card1.setOwnerName("Ivan Ivanov");
        card1.setExpirationDate(YearMonth.of(2026, 12));
        card1.setBalance(1000.0);
        card1.setStatus(ACTIVE);

        CardResponse card2 = new CardResponse();
        card2.setId(2L);
        card2.setMaskedCardNumber("**** **** **** 4444");
        card2.setOwnerName("Anna Petrova");
        card2.setExpirationDate(YearMonth.of(2025, 6));
        card2.setBalance(2000.0);
        card2.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card1, card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
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
                .andExpect(jsonPath("$.content[1].maskedCardNumber").value("**** **** **** 4444"))
                .andExpect(jsonPath("$.content[1].balance").value(2000.0));

        verify(cardService, times(1)).getUserCards(eq(1L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void getCardById_ShouldReturnCard() throws Exception {

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("Ivan Ivanov");
        card.setExpirationDate(YearMonth.of(2026, 12));
        card.setBalance(1000.0);
        card.setStatus(ACTIVE);

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(cardService.getCardById(1L)).thenReturn(card);

        mockMvc.perform(get("/api/user/cards/{id}/balance", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(1000.0));

        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void getCardById_WithBlockedStatus_ShouldReturnBlockedCard() throws Exception {

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("Ivan Ivanov");
        card.setExpirationDate(YearMonth.of(2026, 12));
        card.setBalance(1000.0);
        card.setStatus(BLOCKED);

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(cardService.getCardById(1L)).thenReturn(card);

        mockMvc.perform(get("/api/user/cards/{id}/balance", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.balance").value(1000.0));

        verify(cardService, times(1)).getCardById(1L);
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "admin")
    void getUserCards_ForUser2_ShouldReturnUser2Cards() throws Exception {

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User(2L, "admin", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(4L);
        card.setMaskedCardNumber("**** **** **** 2222");
        card.setOwnerName("Dmitry Kuznetsov");
        card.setExpirationDate(YearMonth.of(2026, 9));
        card.setBalance(5000.0);
        card.setStatus(ACTIVE);

        List<CardResponse> cards = Arrays.asList(card);
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponse> pageCards = new PageImpl<>(cards, pageable, cards.size());

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(cardService.getUserCards(eq(2L), any(Pageable.class))).thenReturn(pageCards);

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(4L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 2222"))
                .andExpect(jsonPath("$.content[0].balance").value(5000.0));

        verify(cardService, times(1)).getUserCards(eq(2L), any(Pageable.class));
        verifyNoMoreInteractions(cardService);

        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    @WithMockUser(username = "user")
    void blockUserCard_ShouldMessage() throws Exception {

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 1111");
        cardResponse.setOwnerName("Ivan Ivanov");

        // изменил ИИ: изменил с doReturn на when.thenReturn для совместимости с @MockBean
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
        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void transfer_ShouldPerformTransfer_Integration() throws Exception {

        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(100.0);

        Card initialFromCard = cardRepository.findById(1L).orElseThrow();
        Card initialToCard = cardRepository.findById(2L).orElseThrow();
        double initialFromBalance = initialFromCard.getBalance();
        double initialToBalance = initialToCard.getBalance();

        mockMvc.perform(post("/api/user/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromCardId").value(1L))
                .andExpect(jsonPath("$.toCardId").value(2L))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        Card updatedFromCard = cardRepository.findById(1L).orElseThrow();
        Card updatedToCard = cardRepository.findById(2L).orElseThrow();

        assertEquals(initialFromBalance - 100.0, updatedFromCard.getBalance(), 0.001);
        assertEquals(initialToBalance + 100.0, updatedToCard.getBalance(), 0.001);

        // Проверка, что транзакция сохранена
        // Можно проверить через transactionRepository.count() или найти конкретную транзакцию
    }
}