// dto/user/UserResponse.java
package com.example.bankcards.dto.user;

import lombok.Data;

import java.util.Set;

/**
 * Добавленный код: DTO для ответа с данными пользователя.
 * Не содержит чувствительных данных (пароля).
 */
@Data
public class UserResponse {
    private Long id;
    private String username;
    private Set<String> roles;
}