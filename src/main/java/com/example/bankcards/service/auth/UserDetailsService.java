// service/auth/UserDetailsService.java
package com.example.bankcards.service.auth;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Кастомная реализация UserDetailsService для загрузки пользователей из БД. Используется Spring Security для аутентификации.
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    // Загружает пользователя по username из БД. Если пользователь не найден, выбрасывает UsernameNotFoundException.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Загрузка пользователя по username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException(
                            "Пользователь с именем '" + username + "' не найден");
                });

        log.info("Пользователь успешно загружен: {} с ролями: {}",
                username, user.getAuthorities().stream().map(Object::toString).toList());
        return user;
    }
}