package com.example.bankcards.dto.user;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Set;
@Data
public class UserRequest {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(max = 50, message = "Имя пользователя не должно превышать 50 символов")
    private String username;
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
    private Set<String> roles;
}