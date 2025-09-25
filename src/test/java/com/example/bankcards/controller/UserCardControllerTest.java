package com.example.bankcards.controller;

import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
import org.springframework.transaction.annotation.Transactional;

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
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // изменил ИИ: изменил с BEFORE_TEST_CLASS на BEFORE_TEST_METHOD для создания схемы перед каждым тестом (решает проблему с сохранением состояния между тестами, обеспечивает вставку данных заново)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // изменил ИИ: изменил с BEFORE_TEST_CLASS на BEFORE_TEST_METHOD для вставки данных перед каждым тестом (обеспечивает наличие карт с id=1,2)
//@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) // изменил ИИ: изменил с AFTER_TEST_CLASS на AFTER_TEST_METHOD для очистки схемы после каждого теста (предотвращает конфликты id от предыдущих запусков)
class UserCardControllerTest {
    @Autowired // изменил ИИ: изменил с @MockBean на @Autowired для transactionRepository, чтобы использовать реальное сохранение в test.transactions
    private TransactionRepository transactionRepository;

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void getUserCards_ShouldReturnUserCards() throws Exception {

        
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

        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("Ivan Ivanov");
        card.setExpirationDate(YearMonth.of(2026, 12));
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

        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void getCardById_WithBlockedStatus_ShouldReturnBlockedCard() throws Exception {

        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse card = new CardResponse();
        card.setId(1L);
        card.setMaskedCardNumber("**** **** **** 1111");
        card.setOwnerName("Ivan Ivanov");
        card.setExpirationDate(YearMonth.of(2026, 12));
        card.setBalance(1000.0);
        card.setStatus(BLOCKED);

        
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

        
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 1111");
        cardResponse.setOwnerName("Ivan Ivanov");

        
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
    @WithMockUser(username = "user", roles = {"USER"})
    @Transactional
    void transfer_ShouldPerformTransfer_Integration() throws Exception {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User(1L, "user", "password", new HashSet<>())));

        // изменил ИИ: добавил проверку количества всех карт для диагностики вставки (ожидается 5: 3 для user, 2 для admin)
        List<Card> allCards = cardRepository.findAll();
        assertEquals(5, allCards.size(), "Ожидалось 5 карт в test.cards после вставки из 002-initial-data-test.sql");

        List<Card> userCards = allCards.stream()
                .filter(card -> card.getUser().getId() == 1L)
                .toList();
        assertEquals(3, userCards.size(), "Ожидалось 3 карты для user_id=1 в test.cards");

        Card initialFromCard = userCards.get(0);
        Card initialToCard = userCards.get(1);
        double initialFromBalance = initialFromCard.getBalance();
        double initialToBalance = initialToCard.getBalance();

        TransactionRequest request = new TransactionRequest();
        request.setFromCardId(initialFromCard.getId());
        request.setToCardId(initialToCard.getId());
        request.setAmount(100.0);

        mockMvc.perform(post("/api/user/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromCardId").value(initialFromCard.getId().intValue()))
                .andExpect(jsonPath("$.toCardId").value(initialToCard.getId().intValue()))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        Card updatedFromCard = cardRepository.findById(initialFromCard.getId()).orElseThrow(() -> new AssertionError("Карта отправителя не найдена после перевода"));
        Card updatedToCard = cardRepository.findById(initialToCard.getId()).orElseThrow(() -> new AssertionError("Карта получателя не найдена после перевода"));

        assertEquals(initialFromBalance - 100.0, updatedFromCard.getBalance(), 0.001, "Баланс карты отправителя не уменьшился на 100.0");
        assertEquals(initialToBalance + 100.0, updatedToCard.getBalance(), 0.001, "Баланс карты получателя не увеличился на 100.0");

        List<Transaction> transactions = transactionRepository.findAll();
        assertEquals(1, transactions.size(), "Транзакция не сохранена в test.transactions");
        Transaction savedTransaction = transactions.get(0);
        assertEquals(initialFromCard.getId(), savedTransaction.getFromCard().getId());
        assertEquals(initialToCard.getId(), savedTransaction.getToCard().getId());
        assertEquals(100.0, savedTransaction.getAmount());
        assertEquals("SUCCESS", savedTransaction.getStatus().name());
    }
}