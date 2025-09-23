package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;

/**
 * добавленный код: Интерфейс для аутентификационного сервиса.
 */
public interface AuthService {
    /**
     * добавленный код: Выполняет аутентификацию пользователя и возвращает JWT-токен.
     * @param authRequest DTO с данными для аутентификации (username, password)
     * @return AuthResponse с JWT-токеном и информацией о пользователе
     */
    AuthResponse authenticate(AuthRequest authRequest);
}