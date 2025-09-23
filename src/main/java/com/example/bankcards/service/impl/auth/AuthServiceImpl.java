package com.example.bankcards.service.impl.auth;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.config.security.JwtUtil;
import com.example.bankcards.service.auth.AuthService;
import com.example.bankcards.service.auth.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * добавленный код: Реализация сервиса для обработки аутентификации.
 * изменил ИИ: Класс переименован из AuthService в AuthServiceImpl и реализует интерфейс AuthService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse authenticate(AuthRequest authRequest) {
        log.debug("Начало процесса аутентификации для пользователя: {}", authRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);

            AuthResponse response = new AuthResponse();
            response.setToken(jwt);
            response.setUsername(userDetails.getUsername());
            response.setExpiration(jwtUtil.getExpirationDateFromToken(jwt).getTime());
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