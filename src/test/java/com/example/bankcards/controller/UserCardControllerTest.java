package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
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
import org.springframework.boot.test.mock.mockito.SpyBean; // добавленный код: импорт для @SpyBean (для использования реальных методов в интеграционном тесте)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles; // добавленный код: импорт для @ActiveProfiles (для активации профиля test)
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
@ActiveProfiles("test") // добавленный код: активация профиля test для использования конфигурации из application-test.yml (default_schema=test, правильная схема БД)
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class UserCardControllerTest {
    @SpyBean // изменил ИИ: изменил с @MockBean на @SpyBean для использования реального метода save (интеграционный тест проверяет изменения в БД)
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private MockMvc mockMvc;

    @SpyBean // изменил ИИ: изменил с @MockBean на @SpyBean для использования реальных методов (в unit-тестах стаббинг через doReturn, в интеграционном - реальные вызовы)
    private CardService cardService;

    @SpyBean // изменил ИИ: изменил с @MockBean на @SpyBean для использования реальной логики transfer (интеграционный тест проверяет изменения баланса)
    private TransactionService transactionService;

    @SpyBean // изменил ИИ: изменил с @MockBean на @SpyBean для использования реального findByUsername в интеграционном тесте (реальные данные из БД)
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void getUserCards_ShouldReturnUserCards() throws Exception {

        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean (избегает вызова реального метода при стаббинге)
        doReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>()))).when(userRepository).findByUsername("user1");

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

        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(pageCards).when(cardService).getUserCards(eq(1L), any(Pageable.class));


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


        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>()))).when(userRepository).findByUsername("user1");

        CardResponse card = new CardResponse();
        card.setId(1L);

        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(1000.0);
        card.setStatus(ACTIVE);

        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(card).when(cardService).getCardById(1L);


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


        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>()))).when(userRepository).findByUsername("user1");

        CardResponse card = new CardResponse();
        card.setId(1L);

        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("User One");
        card.setExpirationDate(YearMonth.of(2025, 12));
        card.setBalance(0.0);
        card.setStatus(BLOCKED);

        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(card).when(cardService).getCardById(1L);


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


        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(Optional.of(new User(2L, "user2", "password", new HashSet<>()))).when(userRepository).findByUsername("user2");

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


        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(pageCards).when(cardService).getUserCards(eq(2L), any(Pageable.class));


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

        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(Optional.of(new User(1L, "user1", "password", new HashSet<>()))).when(userRepository).findByUsername("user1");


        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 1111");
        cardResponse.setOwnerName("User One");


        // изменил ИИ: изменил стаббинг с when на doReturn для совместимости со @SpyBean
        doReturn(cardResponse).when(cardService).getCardById(1L);


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
    @WithMockUser(username = "user") // изменил ИИ: изменил username с "user1" на "user" для соответствия реальным данным из 002-initial-data-test.sql (интеграционный тест использует реальную БД)
    void transfer_ShouldPerformTransfer_Integration() throws Exception {
        // изменил ИИ: удалил закомментированный стаббинг when для userRepository (теперь @SpyBean вызывает реальный метод, пользователь "user" существует в тестовой БД)

        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(100.0);

        // Получаем начальные балансы
        Card initialFromCard = cardRepository.findById(1L).orElseThrow();
        Card initialToCard = cardRepository.findById(2L).orElseThrow();
        double initialFromBalance = initialFromCard.getBalance();
        double initialToBalance = initialToCard.getBalance();

        // Выполнение запроса
        mockMvc.perform(post("/api/user/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromCardId").value(1L))
                .andExpect(jsonPath("$.toCardId").value(2L))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        // Проверка изменений баланса
        Card updatedFromCard = cardRepository.findById(1L).orElseThrow();
        Card updatedToCard = cardRepository.findById(2L).orElseThrow();

        assertEquals(initialFromBalance - 100.0, updatedFromCard.getBalance(), 0.001);
        assertEquals(initialToBalance + 100.0, updatedToCard.getBalance(), 0.001);

        // Проверка, что транзакция сохранена
        // Можно проверить через transactionRepository.count() или найти конкретную транзакцию
    }
}