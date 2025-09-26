package com.example.bankcards.controller;
import com.example.bankcards.dto.user.UserRequest;
import com.example.bankcards.dto.user.UserResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@SecurityScheme(
        name = "bearerAuth",
        type = HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT токен для авторизации. Вставьте: Bearer <токен>"
)
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Управление пользователями (Админ)", description = "Для CRUD с пользователями приложения")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final UserService userService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать пользователя",
            description = "Создает нового пользователя с указанными ролями",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"username\": \"john_doe\", \"password\": \"password123\", \"roles\": [\"USER\"]}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("POST /api/admin/users - Создание пользователя");
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь найден",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id) {
        log.info("GET /api/admin/users/{} - Запрос пользователя", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список пользователей получен",
                            content = @Content(schema = @Schema(implementation = UserResponse[].class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/admin/users - Запрос всех пользователей");
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Обновить пользователя",
            description = "Обновляет данные пользователя по его идентификатору",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления пользователя",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"username\": \"john_doe_updated\", \"password\": \"newpassword123\", \"roles\": [\"USER\"]}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                            content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserRequest userRequest) {
        log.info("PUT /api/admin/users/{} - Обновление пользователя", id);
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаляет пользователя по его идентификатору",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/admin/users/{} - Удаление пользователя", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}