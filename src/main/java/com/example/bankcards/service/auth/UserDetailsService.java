package com.example.bankcards.service.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * добавленный код: Интерфейс для загрузки пользовательских данных для Spring Security.
 */
public interface UserDetailsService extends org.springframework.security.core.userdetails.UserDetailsService {
    /**
     * добавленный код: Загружает пользователя по имени.
     * @param username Имя пользователя
     * @return UserDetails с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}