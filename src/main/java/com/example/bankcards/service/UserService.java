package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;

import java.util.List;

/**
 * добавленный код: Интерфейс для сервиса управления пользователями.
 */
public interface UserService {
    /**
     * добавленный код: Создаёт нового пользователя.
     * @param userRequest DTO с данными пользователя
     * @return UserResponse с данными созданного пользователя
     */
    UserResponse createUser(UserRequest userRequest);

    /**
     * добавленный код: Получает пользователя по ID.
     * @param id ID пользователя
     * @return UserResponse с данными пользователя
     */
    UserResponse getUserById(Long id);

    /**
     * добавленный код: Получает всех пользователей.
     * @return List<UserResponse> со всеми пользователями
     */
    List<UserResponse> getAllUsers();

    /**
     * добавленный код: Обновляет данные пользователя.
     * @param id ID пользователя
     * @param userRequest DTO с обновлёнными данными
     * @return UserResponse с обновлёнными данными пользователя
     */
    UserResponse updateUser(Long id, UserRequest userRequest);

    /**
     * добавленный код: Удаляет пользователя по ID.
     * @param id ID пользователя
     */
    void deleteUser(Long id);
}