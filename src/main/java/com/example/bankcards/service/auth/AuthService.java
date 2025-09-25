package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;


public interface AuthService {
    
    AuthResponse authenticate(AuthRequest authRequest);
}