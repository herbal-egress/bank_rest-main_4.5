// dto/user/UserRequest.java
package com.example.bankcards.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * Добавленный код: DTO для запроса на создание или обновление пользователя.
 * Содержит данные, приходящие от клиента.
 */
@Data
public class UserRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    // Добавленный код: Роли пользователя. Для ADMIN можно назначить роли, для USER обычно только "USER".
    private Set<String> roles;
}