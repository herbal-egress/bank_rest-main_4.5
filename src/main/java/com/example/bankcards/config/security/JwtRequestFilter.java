// config/security/JwtRequestFilter.java
package com.example.bankcards.config.security;

import com.example.bankcards.service.auth.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Добавленный код: Фильтр для проверки JWT токена в каждом HTTP запросе.
 * Извлекает токен из заголовка Authorization, валидирует его и устанавливает аутентификацию.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    // Добавленный код: Сервис для загрузки пользователей.
    private final UserDetailsService userDetailsService;

    // Добавленный код: Утилита для работы с JWT.
    private final JwtUtil jwtUtil;

    /**
     * Добавленный код: Основной метод фильтра. Выполняется для каждого запроса один раз.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        // Добавленный код: Подробное логирование для отладки доступа к Swagger.
        log.debug("=== JWT ФИЛЬТР ===");
        log.debug("Метод: {}, Путь: {}, Authorization: {}",
                request.getMethod(), path,
                authorizationHeader != null ? "присутствует" : "отсутствует");

        // Добавленный код: Логируем, если это запрос к Swagger.
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            log.debug("ЗАПРОС К SWAGGER: {} - пропускаем JWT фильтр", path);
        }

        String username = null;
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwtToken);
            log.debug("Извлечен username из токена: {}", username);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Аутентификация установлена для пользователя: {}", username);
                } else {
                    log.warn("Токен не валиден для пользователя: {}", username);
                }
            } catch (Exception e) {
                log.error("Ошибка при валидации токена для пользователя {}: {}", username, e.getMessage(), e);
            }
        }

        log.debug("=== КОНЕЦ JWT ФИЛЬТРА - передаем управление дальше ===");
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Добавленный код: КРИТИЧЕСКИЙ СПИСОК ИСКЛЮЧЕНИЙ - /auth/login ДОЛЖЕН БЫТЬ ПЕРВЫМ!
        String[] publicPaths = {
                "/auth/login",           // ✅ ЛОГИН - САМЫЙ ПЕРВЫЙ!
                "/auth/**",              // ✅ Все auth пути
                "/swagger-ui.html",      // ✅ Swagger
                "/swagger-ui/**",        // ✅ Swagger ресурсы
                "/v3/api-docs/**",       // ✅ OpenAPI
                "/v3/api-docs",          // ✅ OpenAPI главный
                "/v3/api-docs.yaml",     // ✅ OpenAPI YAML
                "/actuator/health",      // ✅ Health
                "/actuator/**"           // ✅ Actuator
        };

        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || (publicPath.endsWith("**") && path.startsWith(publicPath.replace("/**", "")))) {
                log.debug("🚫 JWT ФИЛЬТР ПРОПУЩЕН: {} (матчит {})", path, publicPath);
                return true;
            }
        }

        log.debug("🔒 JWT ФИЛЬТР ПРИМЕНЁН: {}", path);
        return false;
    }
}