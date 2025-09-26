package com.example.bankcards.controller;
import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.jpa.properties.hibernate.default_schema=test"})
@Sql(scripts = "classpath:db/migration/sql/001-initial-schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/migration/sql/002-initial-data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:db/migration/sql/clear-schema-test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class AdminControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        long userCount = userRepository.count();
        assertEquals(2, userCount, "Ожидалось 2 пользователя в test.users после вставки из 002-initial-data-test.sql");
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("admin"));
    }
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        assertTrue(userRepository.existsById(1L), "Пользователь с id=1 должен существовать в test.users");
        var userOptional = userRepository.findById(1L);
        assertTrue(userOptional.isPresent(), "Пользователь должен быть найден");
        User user = userOptional.get();
        assertFalse(user.getRoles().isEmpty(), "Роли пользователя не должны быть пустыми");
        assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals(Role.RoleType.USER)), "Пользователь должен иметь роль USER");
        mockMvc.perform(get("/api/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles[0]").value("USER")); 
    }
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createUser_ShouldCreateUser() throws Exception {
        long initialUserCount = userRepository.count();
        assertEquals(2, initialUserCount, "Ожидалось 2 пользователя в test.users");
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setPassword("password123");
        userRequest.setRoles(Set.of("USER"));
        mockMvc.perform(post("/api/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.roles[0]").value("USER")); 
        assertEquals(initialUserCount + 1, userRepository.count(), "Количество пользователей должно увеличиться на 1");
        assertTrue(userRepository.findByUsername("newuser").isPresent(), "Новый пользователь должен существовать в БД");
    }
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateUser_ShouldUpdateUser() throws Exception {
        assertTrue(userRepository.existsById(1L), "Пользователь с id=1 должен существовать");
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("updateduser");
        userRequest.setPassword("newpassword123");
        userRequest.setRoles(Set.of("USER"));
        mockMvc.perform(put("/api/admin/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.roles[0]").value("USER")); 
        var updatedUser = userRepository.findById(1L);
        assertTrue(updatedUser.isPresent(), "Обновленный пользователь должен существовать");
        assertEquals("updateduser", updatedUser.get().getUsername(), "Имя пользователя должно быть обновлено");
    }
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @Transactional
    void deleteUser_ShouldDeleteUser() throws Exception {
        long initialUserCount = userRepository.count();
        assertEquals(2, initialUserCount, "Ожидалось 2 пользователя в test.users");
        assertTrue(userRepository.existsById(1L), "Пользователь с id=1 должен существовать");
        long initialCardCount = cardRepository.count();
        assertEquals(5, initialCardCount, "Ожидалось 5 карт в test.cards после вставки из 002-initial-data-test.sql");
        cardRepository.deleteAllCardsByUserId(1L);
        long finalCardCount = cardRepository.count();
        assertEquals(2, finalCardCount, "Ожидалось 2 карты после удаления карт пользователя id=1");
        mockMvc.perform(delete("/api/admin/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertFalse(userRepository.existsById(1L), "Пользователь должен быть удален из БД");
        assertEquals(initialUserCount - 1, userRepository.count(), "Количество пользователей должно уменьшиться на 1");
    }
    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getAllUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}