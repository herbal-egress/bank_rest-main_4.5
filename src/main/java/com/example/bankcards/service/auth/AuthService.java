// service/auth/AuthService.java
package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.config.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Сервисный слой для обработки аутентификации. Координирует работу AuthenticationManager и JwtUtil.
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Выполняет аутентификацию и возвращает JWT токен.
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.debug("Начало процесса аутентификации для пользователя: {}", authRequest.getUsername());

        // Проверяем учетные данные через AuthenticationManager.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        // Если аутентификация успешна, генерируем JWT токен.
        if (authentication.isAuthenticated()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);

            // Создаем ответ с информацией о токене.
            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setUsername(userDetails.getUsername());
            response.setExpiration(jwtUtil.getExpirationDateFromToken(jwt).getTime()); // Дописано: Используем метод для extraction expiration
            response.setRoles(userDetails.getAuthorities().stream()
                    .map(Object::toString)
                    .toArray(String[]::new));

            log.info("Аутентификация завершена успешно для пользователя: {}", authRequest.getUsername());
            return response;
        } else {
            log.error("Аутентификация не удалась для пользователя: {}", authRequest.getUsername());
            throw new BadCredentialsException("Неверные учетные данные");
        }
    }
}