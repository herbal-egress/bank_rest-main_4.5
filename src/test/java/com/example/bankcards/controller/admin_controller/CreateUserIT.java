package com.example.bankcards.controller.admin_controller;

import com.example.bankcards.controller.AdminController;
import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// добавленный код: Юнит-тест для метода createUser в AdminController
@WebMvcTest(AdminController.class)
class CreateUserIT {

    // добавленный код: MockMvc для симуляции HTTP-запросов
    @Autowired
    private MockMvc mockMvc;

    // добавленный код: Мок сервиса UserService
    @MockBean
    private UserService userService;

    // добавленный код: ObjectMapper для сериализации/десериализации JSON
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    // добавленный код: Тест успешного создания пользователя
    void createUser_Success_ReturnsCreated() throws Exception {
        // добавленный код: Подготовка тестовых данных
        UserRequest request = new UserRequest();
        request.setUsername("john_doe");
        request.setPassword("password123");
        request.setRoles(Set.of("USER"));

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("john_doe");
        response.setRoles(Set.of("ROLE_USER"));

        // добавленный код: Настройка мока для метода createUser
        when(userService.createUser(any(UserRequest.class))).thenReturn(response);

        // добавленный код: Выполнение POST-запроса и проверка ответа
        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}