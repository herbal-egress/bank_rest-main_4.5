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

/**
 * Добавленный код: Сервисный слой для обработки аутентификации.
 * Координирует работу AuthenticationManager и JwtUtil.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // Добавленный код: Менеджер аутентификации Spring Security.
    private final AuthenticationManager authenticationManager;

    // Добавленный код: Утилита для работы с JWT.
    private final JwtUtil jwtUtil;

    // Добавленный код: Кастомный UserDetailsService для загрузки пользователей.
    private final CustomUserDetailsService userDetailsService;

    /**
     * Добавленный код: Выполняет аутентификацию и возвращает JWT токен.
     * @param authRequest Данные для аутентификации (username, password)
     * @return AuthResponse с JWT токеном и информацией о пользователе
     * @throws BadCredentialsException если учетные данные неверны
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.debug("Начало процесса аутентификации для пользователя: {}", authRequest.getUsername());

        // Добавленный код: Проверяем учетные данные через AuthenticationManager.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        // Добавленный код: Если аутентификация успешна, генерируем JWT токен.
        if (authentication.isAuthenticated()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);

            // Добавленный код: Создаем ответ с информацией о токене.
            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setUsername(userDetails.getUsername());
            response.setExpiration(System.currentTimeMillis() + jwtUtil.getExpiration()); // Примечание: expiration не публичное, используем геттер или рефлексию
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

    // Добавленный код: Вспомогательный метод для получения времени жизни токена (для совместимости).
    public long getExpirationTime() {
        return jwtUtil.getExpiration(); // Примечание: требует добавления геттера в JwtUtil
    }
}