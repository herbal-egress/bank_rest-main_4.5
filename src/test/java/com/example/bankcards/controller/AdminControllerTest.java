// AdminControllerTest.java
package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:db/changelog/changes/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "classpath:db/changelog/changes/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Arrange
        UserResponse user1 = new UserResponse();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole("USER");

        UserResponse user2 = new UserResponse();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setRole("ADMIN");

        List<UserResponse> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("admin"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"));

        // Verify
        verify(userService, times(1)).getAllUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        // Arrange
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("user1");
        user.setRole("USER");

        when(userService.getUserById(1L)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify
        verify(userService, times(1)).getUserById(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_ShouldCreateUser() throws Exception {
        // Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setPassword("password123");
        userRequest.setRole("USER");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(3L);
        userResponse.setUsername("newuser");
        userResponse.setRole("USER");

        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(header().exists("Location"));

        // Verify
        verify(userService, times(1)).createUser(any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ShouldUpdateUser() throws Exception {
        // Arrange
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("updateduser");
        userRequest.setPassword("newpassword123");
        userRequest.setRole("ADMIN");

        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setUsername("updateduser");
        userResponse.setRole("ADMIN");

        when(userService.updateUser(anyLong(), any(UserRequest.class))).thenReturn(userResponse);

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verify
        verify(userService, times(1)).updateUser(eq(1L), any(UserRequest.class));
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_ShouldDeleteUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify
        verify(userService, times(1)).deleteUser(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getAllUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Verify
        verifyNoInteractions(userService);
    }
}