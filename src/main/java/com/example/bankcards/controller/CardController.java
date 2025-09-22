// controller/CardController.java
package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

/**
 * Добавленный код: REST контроллер для операций с картами.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Карты", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;
    private final UserRepository userRepository;

    @PostMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать карту (Админ)",
            description = "Создает новую банковскую карту",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания карты",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"cardNumber\": \"4532015112830366\", \"ownerName\": \"JOHN DOE\", \"expirationDate\": \"2027-12\", \"balance\": 1000.5, \"userId\": 2}"
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

    @GetMapping("/admin/cards")
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

    @PutMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Обновить карту (Админ)",
            description = "Обновляет данные карты. Можно обновлять только номер карты, имя владельца и срок действия",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления карты",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardUpdateRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"cardNumber\": \"4532015112830366\", \"ownerName\": \"JOHN DOE UPDATED\", \"expirationDate\": \"2028-12\"}"
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

    @DeleteMapping("/admin/cards/{id}")
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

    @GetMapping("/user/cards")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Просмотреть мои карты (юзер)",
            description = "Возвращает список карт текущего аутентифицированного пользователя с пагинацией",
            parameters = {
                    @Parameter(name = "page", description = "С какой карты показать (0 - с первой карты)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
                    @Parameter(name = "size", description = "Какое количество карт показать", example = "10", schema = @Schema(type = "integer", defaultValue = "10")),
                    @Parameter(name = "sort", description = "Поле для сортировки (например, 'id', 'balance')", example = "id", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карт получен",
                            content = @Content(schema = @Schema(implementation = org.springframework.data.domain.Page.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            }
    )
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {

        // Добавленный код: Получаем ID текущего аутентифицированного пользователя
        Long currentUserId = getCurrentUserId();
        log.info("GET /api/user/cards - Запрос карт текущего пользователя ID: {}, page: {}, size: {}", currentUserId, page, size);

        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            pageable = PageRequest.of(page, size, Sort.by(sort));
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<CardResponse> cards = cardService.getUserCards(currentUserId, pageable);
        return ResponseEntity.ok(cards);
    }

    // Добавленный код: Вспомогательный метод для получения ID текущего пользователя
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Попытка доступа без аутентификации");
            throw new AuthenticationException("Пользователь не аутентифицирован");
        }

        // Получаем UserDetails из аутентификации
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Находим пользователя в базе по username чтобы получить ID
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        return user.getId();
    }

//    @GetMapping("/user/cards/{id}")
//    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
//    @Operation(
//            summary = "Получить карту по ID",
//            description = "Возвращает данные конкретной карты пользователя",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Данные карты получены",
//                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<CardResponse> getMyCardById(
//            @Parameter(description = "ID карты", example = "1", required = true)
//            @PathVariable Long id) {
//        log.info("GET /api/user/cards/{} - Запрос карты пользователя", id);
//        CardResponse card = cardService.getCardById(id);
//        return ResponseEntity.ok(card);
//    }

    // изменил ИИ: Изменен путь эндпоинта на /api/admin/cards/{id}/block для соответствия административным эндпоинтам, как в образце /api/admin/cards, и совместимости с правилами SecurityConfig для /api/admin/**
    // изменил ИИ: Изменена аннотация @PreAuthorize на hasRole('ADMIN') для ограничения доступа только администраторам, как в образце для админских эндпоинтов, и соответствия SecurityConfig
    // изменил ИИ: Обновлено summary и description в @Operation для указания на административную операцию, по аналогии с другими админскими методами
    // изменил ИИ: Обновлен лог для соответствия новому пути и роли
    @PostMapping("/admin/cards/{id}/block")
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
    public ResponseEntity<CardResponse> blockMyCard(
            @Parameter(description = "ID карты для блокировки", example = "1", required = true)
            @PathVariable Long id) {
        log.info("POST /api/admin/cards/{}/block - Блокировка карты администратором", id);
        CardResponse blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(blockedCard);
    }

    // изменил ИИ: Изменен путь эндпоинта на /api/admin/cards/{id}/activate для соответствия административным эндпоинтам, как в образце /api/admin/cards, и совместимости с правилами SecurityConfig для /api/admin/**
    // изменил ИИ: Изменена аннотация @PreAuthorize на hasRole('ADMIN') для ограничения доступа только администраторам, как в образце для админских эндпоинтов, и соответствия SecurityConfig
    // изменил ИИ: Обновлено summary и description в @Operation для указания на административную операцию, по аналогии с другими админскими методами
    // изменил ИИ: Обновлен лог для соответствия новому пути и роли
    @PostMapping("/admin/cards/{id}/activate")
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
    public ResponseEntity<CardResponse> activateMyCard(
            @Parameter(description = "ID карты для активации", example = "1", required = true)
            @PathVariable Long id) {
        log.info("POST /api/admin/cards/{}/activate - Активация карты администратором", id);
        CardResponse activatedCard = cardService.activateCard(id);
        return ResponseEntity.ok(activatedCard);
    }
}