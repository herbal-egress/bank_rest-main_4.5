package com.example.bankcards.controller.user_card_controller;

import com.example.bankcards.controller.UserCardController;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// добавленный код: Юнит-тест для метода getCardBalance в UserCardController
@WebMvcTest(UserCardController.class)
class GetCardBalanceIT {

    // добавленный код: MockMvc для симуляции HTTP-запросов
    @Autowired
    private MockMvc mockMvc;

    // добавленный код: Моки зависимостей
    @MockBean
    private CardService cardService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "user", roles = "USER")
    // добавленный код: Тест успешного получения баланса карты
    void getCardBalance_Success_ReturnsOk() throws Exception {
        // добавленный код: Подготовка тестовых данных
        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setMaskedCardNumber("**** **** **** 1234");
        cardResponse.setOwnerName("JOHN DOE");
        cardResponse.setExpirationDate(YearMonth.of(2027, 12));
        cardResponse.setStatus(Card.Status.ACTIVE);
        cardResponse.setBalance(1000.5);
        cardResponse.setUserId(1L);

        // добавленный код: Настройка мока для UserRepository
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        // добавленный код: Настройка мока для CardService
        when(cardService.getCardById(anyLong())).thenReturn(cardResponse);

        // добавленный код: Выполнение GET-запроса и проверка ответа
        mockMvc.perform(get("/api/user/cards/1/balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.5));
    }
}