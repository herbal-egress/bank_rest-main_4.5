
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


@Component
@Slf4j
public class JwtUtil {

    
    @Value("${jwt.secret}")
    private String secret;

    
    @Value("${jwt.expiration}")
    private Long expiration;

    
    public Long getExpiration() {
        return expiration;
    }

    
    public String getSecret() {
        return secret;
    }

    
    private SecretKey getSigningKey() {
        log.debug("Генерация секретного ключа для подписи JWT");
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    
    public String extractUsername(String token) {
        log.debug("Извлечение username из токена: {}", token.substring(0, Math.min(20, token.length())));
        return extractClaim(token, Claims::getSubject);
    }

    
    public Date extractExpiration(String token) {
        log.debug("Извлечение времени истечения токена");
        return extractClaim(token, Claims::getExpiration);
    }

    
    public Date getExpirationDateFromToken(String token) {
        log.debug("Получение даты истечения токена: {}", token.substring(0, Math.min(20, token.length())));
        try {
            return extractExpiration(token);
        } catch (Exception e) {
            log.error("Ошибка извлечения даты истечения токена: {}", e.getMessage());
            throw new IllegalArgumentException("Некорректный формат токена");
        }
    }

    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    
    private Claims extractAllClaims(String token) {
        log.debug("Извлечение всех claims из токена");
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    
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

    
    private Boolean isTokenExpired(String token) {
        boolean isExpired = extractExpiration(token).before(new Date());
        if (isExpired) {
            log.warn("Токен истек: {}", extractExpiration(token));
        }
        return isExpired;
    }

    
    public String generateToken(UserDetails userDetails) {
        log.info("Генерация нового токена для пользователя: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    
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