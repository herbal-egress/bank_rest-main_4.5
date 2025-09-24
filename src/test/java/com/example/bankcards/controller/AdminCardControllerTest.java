package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// добавленный код: Импорты для интеграционного теста, без Mockito.

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.default_schema=test"}) // добавленный код: Схема test для JPA.
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS) // добавленный код: Загрузка схемы.
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS) // добавленный код: Загрузка тестовых данных.
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS) // добавленный код: Очистка схемы.
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setOwnerName("Test Owner");
        cardRequest.setExpirationDate(YearMonth.parse("2025-12"));
        cardRequest.setBalance(500.0);
        cardRequest.setUserId(1L);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.maskedCardNumber").exists()) // Не проверяйте точное значение
                .andExpect(jsonPath("$.ownerName").value("Test Owner"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards() throws Exception {
        mockMvc.perform(get("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8)) // Измените на актуальное количество
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].maskedCardNumber").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardById() throws Exception {
        // Используйте существующий ID из базы (например, 2)
        Long existingCardId = 2L;

        mockMvc.perform(get("/api/admin/cards/{id}", existingCardId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCardId))
                .andExpect(jsonPath("$.maskedCardNumber").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard() throws Exception {
        // Используйте существующий ID
        Long existingCardId = 2L;

        CardUpdateRequest updateRequest = new CardUpdateRequest();
        updateRequest.setStatus("BLOCKED");

        mockMvc.perform(put("/api/admin/cards/{id}", existingCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingCardId))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard() throws Exception {
        // Сначала создайте карту, затем удалите её
        CardRequest cardRequest = new CardRequest();
        cardRequest.setOwnerName("Card to delete");
        cardRequest.setExpirationDate(YearMonth.parse("2025-12"));
        cardRequest.setBalance(100.0);
        cardRequest.setUserId(1L);

        // Создание карты
        MvcResult result = mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long cardId = JsonPath.parse(response).read("$.id", Long.class);

        // Удаление карты
        mockMvc.perform(delete("/api/admin/cards/{id}", cardId))
                .andExpect(status().isNoContent());
    }
}