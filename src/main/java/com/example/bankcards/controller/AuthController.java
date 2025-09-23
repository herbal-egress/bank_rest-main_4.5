// controller/AuthController.java
package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Добавленный код: REST контроллер для обработки запросов аутентификации.
 * Предоставляет эндпоинт для логина и получения JWT токена.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "Для генерации токена по логину/паролю (admin/admin, user/user)")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    // Добавленный код: Сервис для обработки аутентификации.
    private final AuthService authService;

    // Эндпоинт для аутентификации пользователя.
    // Принимает username и password, возвращает JWT токен при успехе.

    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Выполняет вход в систему и возвращает JWT токен"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Получен запрос на аутентификацию для пользователя: {}", authRequest.getUsername());

        try {
            AuthResponse response = authService.authenticate(authRequest);
            log.info("Аутентификация успешна для пользователя: {}", authRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.error("Ошибка аутентификации для пользователя {}: неверные учетные данные", authRequest.getUsername());
            throw new AuthenticationException("Неверное имя пользователя или пароль");
        } catch (AuthenticationException e) {
            log.error("Ошибка аутентификации для пользователя {}: {}", authRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Неожиданная ошибка при аутентификации пользователя {}: {}",
                    authRequest.getUsername(), e.getMessage(), e);
            throw new AuthenticationException("Ошибка сервера при аутентификации");
        }
    }
}