// dto/auth/AuthResponse.java
package com.example.bankcards.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Добавленный код: DTO для ответа на запрос аутентификации.
 * Содержит JWT токен и информацию о пользователе.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // Добавленный код: JWT токен для последующих запросов.
    private String token;

    // Добавленный код: Имя пользователя.
    private String username;

    // Добавленный код: Тип токена (Bearer).
    private String type = "Bearer";

    // Добавленный код: Время истечения токена в миллисекундах (timestamp).
    private long expiration;

    // Добавленный код: Роли пользователя.
    private String[] roles;
}