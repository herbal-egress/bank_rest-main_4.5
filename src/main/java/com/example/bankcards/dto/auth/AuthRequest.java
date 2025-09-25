package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;

    
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}