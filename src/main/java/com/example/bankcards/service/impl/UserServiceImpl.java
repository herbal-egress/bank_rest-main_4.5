package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Запрос на создание пользователя: {}", userRequest.getUsername());

        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка создать пользователя с уже существующим именем: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Пользователь с именем '" + userRequest.getUsername() + "' уже существует");
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        Set<Role> roles = resolveRoles(userRequest.getRoles());
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан с ID: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Запрос пользователя по ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Запрос всех пользователей");
        List<User> users = userRepository.findAll();
        return users.stream().map(this::mapToUserResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        log.info("Запрос на обновление пользователя с ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + id + " не найден"));

        if (!user.getUsername().equals(userRequest.getUsername()) &&
                userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            log.error("Попытка обновить имя пользователя на уже существующее: {}", userRequest.getUsername());
            throw new UsernameAlreadyExistsException("Пользователь с именем '" + userRequest.getUsername() + "' уже существует");
        }

        user.setUsername(userRequest.getUsername());
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        if (userRequest.getRoles() != null) {
            user.setRoles(resolveRoles(userRequest.getRoles()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Пользователь с ID {} успешно обновлен", id);
        return mapToUserResponse(updatedUser);
    }

    @Override
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

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
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