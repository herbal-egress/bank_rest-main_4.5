package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Добавленный код: DTO для запроса аутентификации.
 * Содержит данные для логина (username и password).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    // Добавленный код: Имя пользователя. Обязательное поле, максимум 50 символов.
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;

    // Добавленный код: Пароль. Обязательное поле, без ограничения длины (хранится хешированным).
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}