package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// добавленный код: Аннотация @SpringBootTest загружает полный контекст приложения Spring Boot для интеграционного тестирования контроллера
// добавленный код: @AutoConfigureMockMvc настраивает MockMvc для симуляции HTTP-запросов к контроллеру без запуска сервера
@SpringBootTest
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, value = "/db/changelog/changes/001-initial-schema-test.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS, value = "/db/changelog/changes/002-initial-data-test.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS, value = "/db/changelog/changes/clear-schema-test.sql")
class AdminCardControllerTest {

    // добавленный код: Автосвязывание MockMvc для выполнения запросов к контроллеру
    @Autowired
    private MockMvc mockMvc;

    // добавленный код: Мокирование CardService с помощью @MockBean, чтобы изолировать контроллер от реальной логики сервиса (CardServiceImpl)
    @MockBean
    private CardService cardService;

    // добавленный код: ObjectMapper для сериализации/десериализации объектов в JSON для тестовых запросов/ответов
    @Autowired
    private ObjectMapper objectMapper;

    // добавленный код: Тестовые объекты: CardRequest, CardResponse, CardUpdateRequest для использования в тестах (Card не используется напрямую, так как сервис мокирован)
    private CardRequest cardRequest;
    private CardResponse cardResponse;
    private CardUpdateRequest cardUpdateRequest;

    // добавленный код: Метод @BeforeEach для инициализации тестовых данных перед каждым тестом
    @BeforeEach
    void setUp() {
        // добавленный код: Создание тестового объекта CardRequest с реальными полями из CardRequest (ownerName: String, expirationDate: YearMonth, balance: Double, userId: Long)
        cardRequest = new CardRequest();
        cardRequest.setOwnerName("JOHN DOE");
        cardRequest.setExpirationDate(YearMonth.of(2027, 12));
        cardRequest.setBalance(1000.0);
        cardRequest.setUserId(1L);

        // добавленный код: Создание тестового объекта CardResponse с реальными полями из CardResponse (id: Long, maskedCardNumber: String, ownerName: String, expirationDate: YearMonth, status: Card.Status, balance: Double, userId: Long)
        cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 3456"); // добавленный код: Пример маскированного номера (на основе статического метода maskCardNumber в CardResponse)
        cardResponse.setOwnerName("JOHN DOE");
        cardResponse.setExpirationDate(YearMonth.of(2027, 12));
        cardResponse.setStatus(Card.Status.ACTIVE);
        cardResponse.setBalance(1000.0);
        cardResponse.setUserId(1L);

        // добавленный код: Создание тестового объекта CardUpdateRequest с реальными полями из CardUpdateRequest (ownerName: String, expirationDate: YearMonth)
        cardUpdateRequest = new CardUpdateRequest();
        cardUpdateRequest.setOwnerName("JOHN DOE UPDATED");
        cardUpdateRequest.setExpirationDate(YearMonth.of(2028, 12));
    }

    // добавленный код: Тест для метода createCard (POST /api/admin/cards) из AdminCardController.java
    @Test
    @WithMockUser(roles = "ADMIN") // добавленный код: Симуляция пользователя с ролью ADMIN для проверки @PreAuthorize
    void testCreateCard() throws Exception {
        // добавленный код: Настройка мока для cardService.createCard, возвращающего cardResponse
        when(cardService.createCard(any(CardRequest.class))).thenReturn(cardResponse);

        // добавленный код: Выполнение POST-запроса с JSON-телом cardRequest
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                // добавленный код: Проверка статуса 201 Created как в контроллере
                .andExpect(status().isCreated())
                // добавленный код: Проверка типа содержимого ответа
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка полей JSON-ответа с использованием реальных полей CardResponse
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-12")) // добавленный код: Формат YearMonth.toString()
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.userId").value(1L));

        // добавленный код: Verify вызова createCard ровно один раз с любым CardRequest
        verify(cardService, times(1)).createCard(any(CardRequest.class));
    }

    // добавленный код: Тест для метода getAllCards (GET /api/admin/cards) из AdminCardController.java
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCards() throws Exception {
        // добавленный код: Настройка мока для cardService.getAllCards, возвращающего список с cardResponse
        when(cardService.getAllCards()).thenReturn(Collections.singletonList(cardResponse));

        // добавленный код: Выполнение GET-запроса
        mockMvc.perform(get("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка статуса 200 OK
                .andExpect(status().isOk())
                // добавленный код: Проверка типа содержимого
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка полей первого элемента списка в JSON
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$[0].ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$[0].expirationDate").value("2027-12"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].balance").value(1000.0))
                .andExpect(jsonPath("$[0].userId").value(1L));

        // добавленный код: Verify вызова getAllCards ровно один раз
        verify(cardService, times(1)).getAllCards();
    }

    // добавленный код: Тест для метода updateCard (PUT /api/admin/cards/{id}) из AdminCardController.java
    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCard() throws Exception {
        // добавленный код: Настройка мока для cardService.updateCard
        when(cardService.updateCard(eq(1L), any(CardUpdateRequest.class))).thenReturn(cardResponse);

        // добавленный код: Выполнение PUT-запроса с JSON-телом cardUpdateRequest
        mockMvc.perform(put("/api/admin/cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardUpdateRequest)))
                // добавленный код: Проверка статуса 200 OK
                .andExpect(status().isOk())
                // добавленный код: Проверка типа содержимого
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка полей JSON-ответа
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-12"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.userId").value(1L));

        // добавленный код: Verify вызова updateCard с аргументами 1L и any(CardUpdateRequest) ровно один раз
        verify(cardService, times(1)).updateCard(eq(1L), any(CardUpdateRequest.class));
    }

    // добавленный код: Тест для метода blockCard (POST /api/admin/cards/{id}/block) из AdminCardController.java
    // изменил ИИ: Изменен HTTP-метод с PATCH на POST для соответствия @PostMapping в контроллере (исправление ошибки HttpRequestMethodNotSupportedException)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockCard() throws Exception {
        // добавленный код: Настройка мока для cardService.blockCard
        when(cardService.blockCard(1L)).thenReturn(cardResponse);

        // добавленный код: Выполнение POST-запроса (без тела, так как метод не требует)
        mockMvc.perform(post("/api/admin/cards/1/block")
                        .contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка статуса 200 OK
                .andExpect(status().isOk())
                // добавленный код: Проверка типа содержимого
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка полей JSON-ответа
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-12"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.userId").value(1L));

        // добавленный код: Verify вызова blockCard с 1L ровно один раз
        verify(cardService, times(1)).blockCard(1L);
    }

    // добавленный код: Тест для метода activateCard (POST /api/admin/cards/{id}/activate) из AdminCardController.java
    // изменил ИИ: Изменен HTTP-метод с PATCH на POST для соответствия @PostMapping в контроллере (исправление ошибки HttpRequestMethodNotSupportedException из log.txt)
    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateCard() throws Exception {
        // добавленный код: Настройка мока для cardService.activateCard
        when(cardService.activateCard(1L)).thenReturn(cardResponse);

        // добавленный код: Выполнение POST-запроса (без тела, так как метод не требует)
        mockMvc.perform(post("/api/admin/cards/1/activate")
                        .contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка статуса 200 OK
                .andExpect(status().isOk())
                // добавленный код: Проверка типа содержимого
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка полей JSON-ответа
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-12"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.userId").value(1L));

        // добавленный код: Verify вызова activateCard с 1L ровно один раз
        verify(cardService, times(1)).activateCard(1L);
    }

    // добавленный код: Тест для метода deleteCard (DELETE /api/admin/cards/{id}) из AdminCardController.java
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCard() throws Exception {
        // добавленный код: Настройка мока для cardService.deleteCard (void метод)
        doNothing().when(cardService).deleteCard(1L);

        // добавленный код: Выполнение DELETE-запроса
        mockMvc.perform(delete("/api/admin/cards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // добавленный код: Проверка статуса 204 No Content
                .andExpect(status().isNoContent());

        // добавленный код: Verify вызова deleteCard с 1L ровно один раз
        verify(cardService, times(1)).deleteCard(1L);
    }
}