package com.example.bankcards.controller.admin_card_controller;

import com.example.bankcards.controller.AdminCardController;
import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.YearMonth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// добавленный код: Юнит-тест для метода createCard в AdminCardController
@WebMvcTest(AdminCardController.class)
class CreateCardIT {

    // добавленный код: MockMvc для симуляции HTTP-запросов
    @Autowired
    private MockMvc mockMvc;

    // добавленный код: Мок сервиса CardService
    @MockBean
    private CardService cardService;

    // добавленный код: ObjectMapper для сериализации/десериализации JSON
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    // добавленный код: Тест успешного создания карты
    void createCard_Success_ReturnsCreated() throws Exception {
        // добавленный код: Подготовка тестовых данных
        CardRequest request = new CardRequest();
        request.setOwnerName("JOHN DOE");
        request.setExpirationDate(YearMonth.of(2027, 12));
        request.setBalance(1000.5);
        request.setUserId(2L);

        CardResponse response = new CardResponse();
        response.setId(1L);
        response.setMaskedCardNumber("**** **** **** 1234");
        response.setOwnerName("JOHN DOE");
        response.setExpirationDate(YearMonth.of(2027, 12));
        response.setStatus(Card.Status.ACTIVE);
        response.setBalance(1000.5);
        response.setUserId(2L);

        // добавленный код: Настройка мока для метода createCard
        when(cardService.createCard(any(CardRequest.class))).thenReturn(response);

        // добавленный код: Выполнение POST-запроса и проверка ответа
        mockMvc.perform(post("/api/admin/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.ownerName").value("JOHN DOE"))
                .andExpect(jsonPath("$.expirationDate").value("2027-12"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(1000.5))
                .andExpect(jsonPath("$.userId").value(2));
    }
}