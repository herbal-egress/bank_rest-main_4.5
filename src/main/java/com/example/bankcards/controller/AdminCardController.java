package com.example.bankcards.controller;
import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.service.CardService;
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
@RequestMapping("/api/admin")
@Tag(name = "Операции с картами (Админ)", description = "Для аутентифицированного пользователя с ролью ADMIN")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AdminCardController {
    private final CardService cardService;
    @PostMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать карту (Админ)",
            description = "Создает новую банковскую карту с автоматически сгенерированным номером",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания карты (номер карты генерируется автоматически)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"ownerName\": \"JOHN DOE\", \"expirationDate\": \"2027-12\", \"balance\": 1000.5, \"userId\": 2}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
                    @ApiResponse(responseCode = "400", description = "Неверные данные карты"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        log.info("POST /api/admin/cards - Создание карты администратором");
        CardResponse createdCard = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить все карты (Админ)",
            description = "Возвращает список всех карт в системе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карт получен",
                            content = @Content(schema = @Schema(implementation = CardResponse[].class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<List<CardResponse>> getAllCards() {
        log.info("GET /api/admin/cards - Запрос всех карт администратором");
        List<CardResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }
    @PutMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Обновить карту (Админ)",
            description = "Обновляет данные карты. Можно обновлять только имя владельца и срок действия",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления карты",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"ownerName\": \"JOHN DOE UPDATED\", \"expirationDate\": \"2028-12\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта успешно обновлена",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<CardResponse> updateCard(
            @Parameter(description = "ID карты", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CardUpdateRequest cardUpdateRequest) {
        log.info("PUT /api/admin/cards/{} - Обновление карты администратором", id);
        CardResponse updatedCard = cardService.updateCard(id, cardUpdateRequest);
        return ResponseEntity.ok(updatedCard);
    }
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить карту (Админ)",
            description = "Удаляет карту по ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты для удаления", example = "1", required = true)
            @PathVariable Long id) {
        log.info("DELETE /api/admin/cards/{} - Удаление карты администратором", id);
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Заблокировать карту (Админ)",
            description = "Блокирует карту по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта заблокирована",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<CardResponse> blockCard(
            @Parameter(description = "ID карты для блокировки", example = "1", required = true)
            @PathVariable Long id) {
        log.info("POST /api/admin/cards/{}/block - Блокировка карты администратором", id);
        CardResponse blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(blockedCard);
    }
    @PostMapping("/cards/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Активировать карту (Админ)",
            description = "Активирует заблокированную карту по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карта активирована",
                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<CardResponse> activateCard(
            @Parameter(description = "ID карты для активации", example = "1", required = true)
            @PathVariable Long id) {
        log.info("POST /api/admin/cards/{}/activate - Активация карты администратором", id);
        CardResponse activatedCard = cardService.activateCard(id);
        return ResponseEntity.ok(activatedCard);
    }
}