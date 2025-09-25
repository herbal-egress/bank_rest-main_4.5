package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;

import java.util.List;


public interface UserService {
    
    UserResponse createUser(UserRequest userRequest);

    
    UserResponse getUserById(Long id);

    
    List<UserResponse> getAllUsers();

    
    UserResponse updateUser(Long id, UserRequest userRequest);

    
    void deleteUser(Long id);
}