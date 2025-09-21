// controller/UserController.java
package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

// Глобальная схема безопасности для Bearer токена
@SecurityScheme(
        name = "bearerAuth",
        type = HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT токен для авторизации. Вставьте: Bearer <токен>"
)
/**
 * Добавленный код: REST контроллер для операций с пользователями.
 * Доступен только администраторам.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Пользователи (Админ)", description = "Управление пользователями системы")
@SecurityRequirement(name = "bearerAuth") // Применяет схему безопасности ко всем методам, указывает требование токена в Swagger
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя с указанными ролями")
//    @SecurityRequirement(name = "bearerAuth") // Явно указывает требование токена в Swagger
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("POST /api/admin/users - Создание пользователя");
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает данные пользователя по его идентификатору")
//    @SecurityRequirement(name = "bearerAuth") // Явно указывает требование токена
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/admin/users/{} - Запрос пользователя", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей")
//    @SecurityRequirement(name = "bearerAuth") // Явно указывает требование токена в Swagger
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/admin/users - Запрос всех пользователей");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по его идентификатору")
//    @SecurityRequirement(name = "bearerAuth") // Явно указывает требование токена в Swagger
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        log.info("PUT /api/admin/users/{} - Обновление пользователя", id);
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его идентификатору")
//    @SecurityRequirement(name = "bearerAuth") // Явно указывает требование токена в Swagger
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/admin/users/{} - Удаление пользователя", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}