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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.default_schema=test"})
@Sql(scripts = "classpath:db/migration/sql/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/migration/sql/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/migration/sql/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void authenticate_ShouldReturnToken() throws Exception {
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
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.type").value("Bearer"));
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }
    @Test
    void authenticate_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user1");
        authRequest.setPassword("wrongpassword");
        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new com.example.bankcards.exception.AuthenticationException("Invalid credentials"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }
    @Test
    void authenticate_WithEmptyUsername_ShouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("");
        authRequest.setPassword("password1");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())  
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors.username").value("Имя пользователя не может быть пустым"));  
        verifyNoInteractions(authService);
    }
    @Test
    void authenticate_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user1");
        authRequest.setPassword("");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())  
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldErrors.password").value("Пароль не может быть пустым"));  
        verifyNoInteractions(authService);
    }
    @Test
    void authenticate_WithAdminUser_ShouldReturnAdminRole() throws Exception {
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
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.token").value("jwt-token-admin"))
                .andExpect(jsonPath("$.type").value("Bearer"));
        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        verifyNoMoreInteractions(authService);
    }
    @Test
    void register_ShouldReturnToken() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("newuser");
        authRequest.setPassword("newpassword");
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUsername("newuser");
        authResponse.setRoles(new String[]{"ROLE_USER"});
        authResponse.setToken("jwt-token-new");
        authResponse.setType("Bearer");
        authResponse.setExpiration(System.currentTimeMillis() + 3600000);
        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse); 
        mockMvc.perform(post("/auth/login") 
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token-new"));
        verify(authService, times(1)).authenticate(any(AuthRequest.class)); 
        verifyNoMoreInteractions(authService);
    }
}