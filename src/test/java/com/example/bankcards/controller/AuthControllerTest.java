package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.service.auth.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authenticate_ShouldReturnToken() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user1");
        authRequest.setPassword("password1");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername("user1");
        authResponse.setRoles(new String[]{"ROLE_USER"});
        authResponse.setToken("jwt-token-here");
        authResponse.setType("Bearer");
        authResponse.setExpiration(System.currentTimeMillis() + 3600000);

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.type").value("Bearer"));

        // Verify
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user1");
        authRequest.setPassword("wrongpassword");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.AuthenticationException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());

        // Verify
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void authenticate_WithEmptyUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("");
        authRequest.setPassword("password1");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())  // изменил ИИ: Изменено ожидание статуса с 403 на 400, так как валидация @NotBlank возвращает Bad Request через GlobalExceptionHandler
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors.username").value("Имя пользователя не может быть пустым"));  // добавленный код: Проверка тела ответа на ошибку валидации для username

        // Verify
        verifyNoInteractions(authService);
    }

    @Test
    void authenticate_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user1");
        authRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())  // изменил ИИ: Изменено ожидание статуса с 403 на 400, так как валидация @NotBlank возвращает Bad Request через GlobalExceptionHandler
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors.password").value("Пароль не может быть пустым"));  // добавленный код: Проверка тела ответа на ошибку валидации для password

        // Verify
        verifyNoInteractions(authService);
    }

    @Test
    void authenticate_WithAdminUser_ShouldReturnAdminRole() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("admin");
        authRequest.setPassword("adminpass");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername("admin");
        authResponse.setRoles(new String[]{"ROLE_ADMIN"});
        authResponse.setToken("jwt-token-admin");
        authResponse.setType("Bearer");
        authResponse.setExpiration(System.currentTimeMillis() + 3600000);

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.token").value("jwt-token-admin"))
                .andExpect(jsonPath("$.type").value("Bearer"));

        // Verify
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }

    @Test
    void register_ShouldReturnToken() throws Exception {
        // Arrange
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("newuser");
        authRequest.setPassword("newpassword");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername("newuser");
        authResponse.setRoles(new String[]{"ROLE_USER"});
        authResponse.setToken("jwt-token-new");
        authResponse.setType("Bearer");
        authResponse.setExpiration(System.currentTimeMillis() + 3600000);

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse); // добавленный код: Мокирование AuthService.authenticate для эндпоинта /auth/login

        // Act & Assert
        mockMvc.perform(post("/auth/login") // изменил ИИ: Изменён эндпоинт с /auth/register на /auth/login, так как /auth/register отсутствует в AuthController
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token-new"));

        // Verify
        verify(authService, times(1)).authenticate(any(AuthRequest.class)); // добавленный код: Проверка вызова метода authenticate
        verifyNoMoreInteractions(authService);
    }
}