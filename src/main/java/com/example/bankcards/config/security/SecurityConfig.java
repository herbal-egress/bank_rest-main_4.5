// config/security/SecurityConfig.java - КРИТИЧЕСКИЕ ИСПРАВЛЕНИЯ
package com.example.bankcards.config.security;

import com.example.bankcards.service.auth.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
@Primary // добавленный код: Обеспечивает приоритет этой конфигурации
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Добавленный код: КРИТИЧЕСКАЯ ИСПРАВЛЕННАЯ КОНФИГУРАЦИЯ
     * Порядок правил ВАЖЕН: permitAll ДОДЖНЫ идти ПЕРВЫМИ!
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== КРИТИЧЕСКАЯ НАСТРОЙКА БЕЗОПАСНОСТИ ===");

        http
                // Добавленный код: ОТКЛЮЧАЕМ CSRF ПЕРВЫМ ДЕЛОМ
                .csrf(csrf -> {
                    log.info("CSRF защита ОТКЛЮЧЕНА");
                    csrf.disable();
                })

                // Добавленный код: CORS ВТОРЫМ
                .cors(cors -> {
                    log.info("CORS настроен");
                    cors.configurationSource(corsConfigurationSource());
                })

                // Добавленный код: SESSION MANAGEMENT ТРЕТЬИМ
                .sessionManagement(session -> {
                    log.info("Сессии отключены (STATELESS)");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

                // Добавленный код: КРИТИЧЕСКИЙ ПОРЯДОК ПРАВИЛ АВТОРИЗАЦИИ
                .authorizeHttpRequests(authz -> authz
                        // Добавленный код: 1. ПУБЛИЧНЫЕ ЭНДПОИНТЫ (permitAll ПЕРВЫМИ!)
                        .requestMatchers(
                                "/auth/login",           // ✅ ЛОГИН - ДОЛЖЕН БЫТЬ ПЕРВЫМ!
                                "/auth/**",              // ✅ Все auth эндпоинты
                                "/swagger-ui.html",      // ✅ Swagger главная
                                "/swagger-ui/**",        // ✅ Swagger ресурсы
                                "/v3/api-docs/**",       // ✅ OpenAPI docs
                                "/v3/api-docs",          // ✅ OpenAPI главный
                                "/v3/api-docs.yaml",     // ✅ OpenAPI YAML
                                "/actuator/health",      // ✅ Health check
                                "/actuator/**"           // ✅ Все actuator
                        ).permitAll()

                        // Добавленный код: 2. РОЛЕВЫЕ ЭНДПОИНТЫ (ПОСЛЕ permitAll)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // Добавленный код: 3. ВСЕ ОСТАЛЬНОЕ ТРЕБУЕТ АУТЕНТИФИКАЦИЮ
                        .anyRequest().authenticated()
                )

                // Добавленный код: JWT ФИЛЬТР ПОСЛЕДНИМ (после authorizeHttpRequests!)
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("=== КОНФИГУРАЦИЯ БЕЗОПАСНОСТИ ПРИМЕНЕНА ===");
        log.info("permitAll для /auth/login: ✅ ВКЛЮЧЕНО");
        log.info("CSRF: ❌ ОТКЛЮЧЕНО");
        log.info("Sessions: ❌ STATELESS");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Добавленный код: AuthenticationProvider ДОЛЖЕН быть @Bean
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        log.info("AuthenticationProvider НАСТРОЕН с BCrypt");
        return authProvider;
    }

    /**
     * Добавленный код: AuthenticationManager ДОЛЖЕН быть @Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        AuthenticationManager manager = config.getAuthenticationManager();
        log.info("AuthenticationManager СОЗДАН");
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        log.info("BCryptPasswordEncoder ИНИЦИАЛИЗИРОВАН (strength=12)");
        return encoder;
    }
}