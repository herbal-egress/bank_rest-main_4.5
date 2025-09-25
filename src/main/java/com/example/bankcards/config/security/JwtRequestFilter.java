
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


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    
    private final UserDetailsService userDetailsService;

    
    private final JwtUtil jwtUtil;

    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String path = request.getRequestURI();

        
        log.debug("=== JWT ФИЛЬТР ===");
        log.debug("Метод: {}, Путь: {}, Authorization: {}",
                request.getMethod(), path,
                authorizationHeader != null ? "присутствует" : "отсутствует");

        
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
    public boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        
        String[] publicPaths = {
                "/auth/login",           
                "/auth/**",              
                "/swagger-ui.html",      
                "/swagger-ui/**",        
                "/v3/api-docs/**",       
                "/v3/api-docs",          
                "/v3/api-docs.yaml",     
                "/actuator/health",      
                "/actuator/**"           
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