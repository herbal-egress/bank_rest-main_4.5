// AuthControllerTest.java
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
        authResponse.setRole("ROLE_USER");
        authResponse.setToken("jwt-token-here");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"));

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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

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
        authResponse.setRole("ROLE_ADMIN");
        authResponse.setToken("jwt-token-admin");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.token").value("jwt-token-admin"));

        // Verify
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }
}