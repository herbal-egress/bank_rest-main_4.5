// service/UserService.java
package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Добавленный код: Сервисный слой для бизнес-логики работы с пользователями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Будет создан ниже
    private final PasswordEncoder passwordEncoder;

    /**
     * Добавленный код: Создание нового пользователя.
     * Шифрует пароль, проверяет уникальность username, назначает роли.
     */
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Запрос на создание пользователя: {}", userRequest.getUsername());

        // Проверка на существующего пользователя
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка создать пользователя с уже существующим именем: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Пользователь с именем '" + userRequest.getUsername() + "' уже существует");
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Шифруем пароль

        // Назначаем роли
        Set<Role> roles = resolveRoles(userRequest.getRoles());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан с ID: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    /**
     * Добавленный код: Получение пользователя по ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Запрос пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new UserNotFoundException("Пользователь с ID " + id + " не найден");
                });
        return mapToUserResponse(user);
    }

    /**
     * Добавленный код: Получение всех пользователей.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userRepository.findAll();
        log.info("Найдено {} пользователей", users.size());
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Добавленный код: Обновление данных пользователя.
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));

        // Проверяем, не занят ли новый username другим пользователем
        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка обновить username на уже занятый: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Имя пользователя '" + userRequest.getUsername() + "' уже занято");
        }

        user.setUsername(userRequest.getUsername());
        // Обновляем пароль, только если он предоставлен и не пустой
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            log.debug("Пароль пользователя ID {} обновлен", id);
        }

        // Обновляем роли
        Set<Role> roles = resolveRoles(userRequest.getRoles());
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно обновлен", id);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Добавленный код: Удаление пользователя по ID.
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Попытка удалить несуществующего пользователя с ID: {}", id);
            throw new UserNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }

    /**
     * Добавленный код: Вспомогательный метод для преобразования User в UserResponse.
     */
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }

    /**
     * Добавленный код: Преобразует названия ролей из String в сущности Role.
     * Если роли не указаны, назначается роль USER по умолчанию.
     */
    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            // Роль по умолчанию
            Role defaultRole = roleRepository.findByName(Role.RoleType.USER)
                    .orElseThrow(() -> new IllegalStateException("Роль USER не найдена в базе данных"));
            roles.add(defaultRole);
            log.debug("Назначена роль по умолчанию: USER");
        } else {
            for (String roleName : roleNames) {
                Role.RoleType roleType;
                try {
                    roleType = Role.RoleType.valueOf(roleName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.error("Указана несуществующая роль: {}", roleName);
                    throw new IllegalArgumentException("Роль '" + roleName + "' не существует");
                }
                Role role = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new IllegalStateException("Роль " + roleType + " не найдена в базе данных"));
                roles.add(role);
            }
            log.debug("Назначены роли: {}", roleNames);
        }
        return roles;
    }
}