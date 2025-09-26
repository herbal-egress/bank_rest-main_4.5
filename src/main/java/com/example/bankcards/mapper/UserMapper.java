package com.example.bankcards.mapper;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
@Component 
public class UserMapper {
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