package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

// добавленный код: Класс маппера для преобразования сущности User в DTO UserResponse
@Component // добавленный код: Аннотация для регистрации как Spring-бин
public class UserMapper {

    // добавленный код: Преобразование сущности User в DTO UserResponse
    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return response;
    }
}