// config/security/JwtUtil.java
package com.example.bankcards.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

// Утилитный класс для работы с JWT токенами. Обрабатывает создание, валидацию и извлечение информации из токенов.
@Component
@Slf4j
public class JwtUtil {

    // Секретный ключ для подписи токенов. В продакшене должен быть в переменных окружения.
    @Value("${jwt.secret}")
    private String secret;

    // Время жизни токена в миллисекундах (24 часа по умолчанию).
    @Value("${jwt.expiration}")
    private Long expiration;

    // Геттер для времени жизни токена (для совместимости с AuthService).
    public Long getExpiration() {
        return expiration;
    }

    // Геттер для секретного ключа (только для чтения).
    public String getSecret() {
        return secret;
    }

    // Генерирует секретный ключ на основе строки secret. Используется HS512 алгоритм.
    private SecretKey getSigningKey() {
        log.debug("Генерация секретного ключа для подписи JWT");
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Извлекает username из токена без проверки валидности.
    public String extractUsername(String token) {
        log.debug("Извлечение username из токена: {}", token.substring(0, Math.min(20, token.length())));
        return extractClaim(token, Claims::getSubject);
    }

    // Извлекает время истечения токена.
    public Date extractExpiration(String token) {
        log.debug("Извлечение времени истечения токена");
        return extractClaim(token, Claims::getExpiration);
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Извлекает дату истечения токена (для совместимости с AuthService).
    public Date getExpirationDateFromToken(String token) {
        log.debug("Получение даты истечения токена: {}", token.substring(0, Math.min(20, token.length())));
        try {
            return extractExpiration(token);
        } catch (Exception e) {
            log.error("Ошибка извлечения даты истечения токена: {}", e.getMessage());
            throw new IllegalArgumentException("Некорректный формат токена");
        }
    }

    // Извлекает произвольное значение из claims токена.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Извлекает все claims из токена с проверкой подписи.
    private Claims extractAllClaims(String token) {
        log.debug("Извлечение всех claims из токена");
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Проверяет валидность токена для конкретного пользователя.
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        if (isValid) {
            log.debug("Токен валиден для пользователя: {}", username);
        } else {
            log.warn("Токен невалиден для пользователя: {}", username);
        }
        return isValid;
    }

    // Проверяет, истек ли токен.
    private Boolean isTokenExpired(String token) {
        boolean isExpired = extractExpiration(token).before(new Date());
        if (isExpired) {
            log.warn("Токен истек: {}", extractExpiration(token));
        }
        return isExpired;
    }

    // Генерирует новый JWT токен для UserDetails. Включает username и роли.
    public String generateToken(UserDetails userDetails) {
        log.info("Генерация нового токена для пользователя: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // Создает токен с дополнительными claims. Устанавливает время истечения.
    private String createToken(Map<String, Object> claims, String subject) {
        log.debug("Создание токена для subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Проверяет, начинается ли токен с префикса "Bearer ".
    public Boolean isTokenValid(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Некорректный формат токена: {}", token != null ? token.substring(0, Math.min(20, token.length())) : "null");
            return false;
        }
        String jwtToken = token.substring(7);
        try {
            extractAllClaims(jwtToken);
            log.debug("Токен валиден по формату и подписи");
            return true;
        } catch (Exception e) {
            log.error("Ошибка валидации токена: {}", e.getMessage(), e);
            return false;
        }
    }
}