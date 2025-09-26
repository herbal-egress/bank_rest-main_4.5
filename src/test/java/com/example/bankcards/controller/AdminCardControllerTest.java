package com.example.bankcards.controller;
import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
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
import java.time.YearMonth;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.default_schema=test"})
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminCardControllerTest {
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard() throws Exception {
        long initialCardCount = cardRepository.count();
        assertEquals(5, initialCardCount, "Ожидалось 5 карт в test.cards после вставки из 002-initial-data-test.sql");
        CardRequest cardRequest = new CardRequest();
        cardRequest.setOwnerName("Test Owner");
        cardRequest.setExpirationDate(YearMonth.parse("2025-12"));
        cardRequest.setBalance(500.0);
        cardRequest.setUserId(1L);
        MvcResult result = mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.maskedCardNumber").exists())
                .andExpect(jsonPath("$.maskedCardNumber").isString())
                .andExpect(jsonPath("$.ownerName").value("Test Owner"))
                .andExpect(jsonPath("$.expirationDate").value("2025-12"))
                .andExpect(jsonPath("$.balance").value(500.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userId").value(1))
                .andReturn();
        long finalCardCount = cardRepository.count();
        assertEquals(initialCardCount + 1, finalCardCount, "Количество карт должно увеличиться на 1");
        String response = result.getResponse().getContentAsString();
        Long cardId = JsonPath.parse(response).read("$.id", Long.class);
        assertTrue(cardRepository.existsById(cardId), "Созданная карта должна существовать в БД");
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards() throws Exception {
        long actualCardCount = cardRepository.count();
        assertEquals(5, actualCardCount, "Ожидалось 5 карт в test.cards после вставки из 002-initial-data-test.sql");
        mockMvc.perform(get("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(actualCardCount))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].maskedCardNumber").exists())
                .andExpect(jsonPath("$[0].maskedCardNumber").isString())
                .andExpect(jsonPath("$[0].ownerName").exists())
                .andExpect(jsonPath("$[0].ownerName").isString())
                .andExpect(jsonPath("$[0].expirationDate").exists())
                .andExpect(jsonPath("$[0].expirationDate").isString())
                .andExpect(jsonPath("$[0].balance").exists())
                .andExpect(jsonPath("$[0].balance").isNumber())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].status").isString())
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].userId").isNumber());
        assertTrue(actualCardCount >= 0, "Количество карт должно быть неотрицательным");
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCard() throws Exception {
        Long existingCardId = 2L;
        assertTrue(cardRepository.existsById(existingCardId), "Карта для обновления должна существовать");
        var originalCard = cardRepository.findById(existingCardId);
        assertTrue(originalCard.isPresent(), "Исходная карта должна существовать");
        String updateRequestJson = """
                {
                    "ownerName": "UPDATED OWNER NAME",
                    "expirationDate": "2026-12"
                }
                """;
        mockMvc.perform(put("/api/admin/cards/{id}", existingCardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(existingCardId))
                .andExpect(jsonPath("$.ownerName").value("UPDATED OWNER NAME"))
                .andExpect(jsonPath("$.expirationDate").value("2026-12"))
                .andExpect(jsonPath("$.maskedCardNumber").exists())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.userId").exists());
        var updatedCard = cardRepository.findById(existingCardId);
        assertTrue(updatedCard.isPresent(), "Обновленная карта должна существовать");
        assertEquals("UPDATED OWNER NAME", updatedCard.get().getOwnerName(), "Имя владельца должно быть обновлено");
        assertEquals(YearMonth.parse("2026-12"), updatedCard.get().getExpirationDate(), "Дата expiration должна быть обновлена");
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard() throws Exception {
        long initialCardCount = cardRepository.count();
        assertEquals(5, initialCardCount, "Ожидалось 5 карт в test.cards после вставки из 002-initial-data-test.sql");
        CardRequest cardRequest = new CardRequest();
        cardRequest.setOwnerName("Card to delete");
        cardRequest.setExpirationDate(YearMonth.parse("2025-12"));
        cardRequest.setBalance(100.0);
        cardRequest.setUserId(1L);
        MvcResult result = mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        Long cardId = JsonPath.parse(response).read("$.id", Long.class);
        assertTrue(cardRepository.existsById(cardId), "Созданная карта должна существовать перед удалением");
        assertEquals(initialCardCount + 1, cardRepository.count(), "Количество карт должно увеличиться после создания");
        mockMvc.perform(delete("/api/admin/cards/{id}", cardId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertFalse(cardRepository.existsById(cardId), "Карта должна быть удалена из БД");
        assertEquals(initialCardCount, cardRepository.count(), "Количество карт должно вернуться к исходному значению");
    }
}