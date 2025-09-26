package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RoleRepository roleRepository;
    @MockBean
    private PasswordEncoder passwordEncoder;
    private UserRequest userRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setUsername("newuser");
        userRequest.setPassword("password");
        userRequest.setRoles(Set.of("USER"));
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("user");
        mockUser.setPassword("encodedPassword");
        mockUser.setRoles(Set.of(createRole(Role.RoleType.USER)));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByName(Role.RoleType.USER)).thenReturn(Optional.of(createRole(Role.RoleType.USER)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createUser_Success() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("newuser");
        createdUser.setPassword("encodedPassword");
        createdUser.setRoles(Set.of(createRole(Role.RoleType.USER)));
        when(userRepository.save(any(User.class))).thenReturn(createdUser);
        UserResponse response = userService.createUser(userRequest);
        assertNotNull(response.getId());
        assertEquals("newuser", response.getUsername());
        assertTrue(response.getRoles().contains("USER"));
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createUser_UsernameExists_ThrowsException() {
        userRequest.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(userRequest));
        verify(userRepository, times(1)).findByUsername("user");
    }

    @Test
    @WithMockUser(username = "user")
    void getUserById_Success() {
        UserResponse response = userService.getUserById(1L);
        assertEquals(1L, response.getId());
        assertEquals("user", response.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        List<UserResponse> response = userService.getAllUsers();
        assertEquals(1, response.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(username = "user")
    void updateUser_Success() {
        userRequest.setUsername("updateduser");
        userRequest.setPassword("newpassword");
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(userRepository.findByUsername("updateduser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        UserResponse response = userService.updateUser(1L, userRequest);
        assertEquals("updateduser", response.getUsername());
        verify(passwordEncoder, times(1)).encode("newpassword");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    private Role createRole(Role.RoleType roleType) {
        Role role = new Role();
        role.setName(roleType);
        return role;
    }
}