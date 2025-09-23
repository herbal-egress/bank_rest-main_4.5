package com.example.bankcards.controller.auth_controller;

import com.example.bankcards.config.security.JwtUtil;
import com.example.bankcards.config.security.JwtRequestFilter;
import com.example.bankcards.controller.AuthController;
import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.service.auth.AuthService;
import com.example.bankcards.service.auth.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// добавленный код: Юнит-тест для метода login в AuthController
@WebMvcTest(AuthController.class)
class LoginIT {

    // добавленный код: MockMvc для симуляции HTTP-запросов
    @Autowired
    private MockMvc mockMvc;

    // добавленный код: Мок сервиса AuthService
    @MockBean
    private AuthService authService;

    // добавленный код: Мок для UserDetailsService (зависимость SecurityConfig и AuthServiceImpl)
    @MockBean
    private UserDetailsService userDetailsService;

    // добавленный код: Мок для JwtRequestFilter (зависимость SecurityConfig)
    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    // добавленный код: Мок для AuthenticationManager (зависимость AuthServiceImpl)
    @MockBean
    private AuthenticationManager authenticationManager;

    // добавленный код: Мок для JwtUtil (зависимость JwtRequestFilter)
    @MockBean
    private JwtUtil jwtUtil;

    // добавленный код: ObjectMapper для сериализации/десериализации JSON
    @Autowired
    private ObjectMapper objectMapper;

    @Test
        // добавленный код: Тест успешной аутентификации
    void login_Success_ReturnsOk() throws Exception {
        // добавленный код: Подготовка тестовых данных
        AuthRequest request = new AuthRequest("admin", "admin");
        AuthResponse response = new AuthResponse();
        response.setToken("jwt-token");
        response.setUsername("admin");
        response.setType("Bearer");
        response.setExpiration(System.currentTimeMillis() + 86400000);
        response.setRoles(new String[]{"ROLE_ADMIN"});

        // добавленный код: Настройка мока для метода authenticate
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        // добавленный код: Настройка мока для JwtRequestFilter, чтобы пропускать /auth/login
        when(jwtRequestFilter.shouldNotFilter(any(HttpServletRequest.class))).thenReturn(true);

        // добавленный код: Выполнение POST-запроса и проверка ответа
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.anonymous())) // изменил ИИ: Добавляет анонимный контекст для запроса, обходя security для permitAll путей
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }
}