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

/**
 * Добавленный код: Утилитный класс для работы с JWT токенами.
 * Обрабатывает создание, валидацию и извлечение информации из токенов.
 */
@Component
@Slf4j
public class JwtUtil {

    // Добавленный код: Секретный ключ для подписи токенов. В продакшене должен быть в переменных окружения.
    @Value("${jwt.secret}")
    private String secret;

    // Добавленный код: Время жизни токена в миллисекундах (24 часа по умолчанию).
    @Value("${jwt.expiration}")
    private Long expiration;

    // Дополнение для AuthService: Геттер для времени жизни токена (для совместимости с AuthService).
    public Long getExpiration() {
        return expiration;
    }

    // Добавленный код: Геттер для секретного ключа (только для чтения).
    public String getSecret() {
        return secret;
    }

    // Добавленный код: Генерирует секретный ключ на основе строки secret. Используется HS512 алгоритм.
    private SecretKey getSigningKey() {
        log.debug("Генерация секретного ключа для подписи JWT");
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Добавленный код: Извлекает username из токена без проверки валидности.
    public String extractUsername(String token) {
        log.debug("Извлечение username из токена: {}", token.substring(0, Math.min(20, token.length())));
        return extractClaim(token, Claims::getSubject);
    }

    // Добавленный код: Извлекает время истечения токена.
    public Date extractExpiration(String token) {
        log.debug("Извлечение времени истечения токена");
        return extractClaim(token, Claims::getExpiration);
    }

    // Добавленный код: Универсальный метод для извлечения claim из токена.
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Добавленный код: Извлекает все claims из токена с проверкой подписи.
    private Claims extractAllClaims(String token) {
        log.debug("Извлечение всех claims из токена");
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Добавленный код: Проверяет валидность токена по UserDetails. Возвращает true, если токен действителен.
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        log.debug("Валидация токена для пользователя: {}", username);
        boolean isTokenValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        if (isTokenValid) {
            log.info("Токен успешно валидирован для пользователя: {}", username);
        } else {
            log.warn("Токен НЕ валиден для пользователя: {}. Username: {}, Expired: {}",
                    userDetails.getUsername(), username, isTokenExpired(token));
        }
        return isTokenValid;
    }

    // Добавленный код: Проверяет, истек ли токен.
    private Boolean isTokenExpired(String token) {
        boolean isExpired = extractExpiration(token).before(new Date());
        if (isExpired) {
            log.warn("Токен истек: {}", extractExpiration(token));
        }
        return isExpired;
    }

    // Добавленный код: Генерирует новый JWT токен для UserDetails. Включает username и роли.
    public String generateToken(UserDetails userDetails) {
        log.info("Генерация нового токена для пользователя: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // Добавленный код: Создает токен с дополнительными claims. Устанавливает время истечения.
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

    // Добавленный код: Проверяет, начинается ли токен с префикса "Bearer ".
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