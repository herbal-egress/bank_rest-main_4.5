//// controller/CardController.java
//package com.example.bankcards.controller;
//
//import com.example.bankcards.dto.card.CardRequest;
//import com.example.bankcards.dto.card.CardResponse;
//import com.example.bankcards.dto.card.CardUpdateRequest;
//import com.example.bankcards.entity.User;
//import com.example.bankcards.exception.AuthenticationException;
//import com.example.bankcards.exception.UserNotFoundException;
//import com.example.bankcards.repository.UserRepository;
//import com.example.bankcards.service.CardService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.ExampleObject;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;
//
//@SecurityScheme(
//        name = "bearerAuth",
//        type = HTTP,
//        scheme = "bearer",
//        bearerFormat = "JWT",
//        description = "JWT токен для авторизации. Вставьте: Bearer <токен>"
//)
//
///**
// * REST контроллер для операций с картами.
// */
//@RestController
//@RequestMapping("/api")
//@Tag(name = "Карты", description = "Управление банковскими картами")
//@SecurityRequirement(name = "bearerAuth")
//@RequiredArgsConstructor
//@Slf4j
//public class CardController {
//
//    private final CardService cardService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/admin/cards")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Создать карту (Админ)",
//            description = "Создает новую банковскую карту",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Данные для создания карты",
//                    required = true,
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = CardRequest.class),
//                            examples = @ExampleObject(
//                                    value = "{\"cardNumber\": \"4532015112830366\", \"ownerName\": \"JOHN DOE\", \"expirationDate\": \"2027-12\", \"balance\": 1000.5, \"userId\": 2}"
//                            )
//                    )
//            ),
//            responses = {
//                    @ApiResponse(responseCode = "201", description = "Карта успешно создана"),
//                    @ApiResponse(responseCode = "400", description = "Неверные данные карты"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
//        log.info("POST /api/admin/cards - Создание карты администратором");
//        CardResponse createdCard = cardService.createCard(cardRequest);
//        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
//    }
//
//    @GetMapping("/admin/cards")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Получить все карты (Админ)",
//            description = "Возвращает список всех карт в системе",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Список карт получен",
//                            content = @Content(schema = @Schema(implementation = CardResponse[].class))),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<List<CardResponse>> getAllCards() {
//        log.info("GET /api/admin/cards - Запрос всех карт администратором");
//        List<CardResponse> cards = cardService.getAllCards();
//        return ResponseEntity.ok(cards);
//    }
//
//    @PutMapping("/admin/cards/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Обновить карту (Админ)",
//            description = "Обновляет данные карты. Можно обновлять только номер карты, имя владельца и срок действия",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Данные для обновления карты",
//                    required = true,
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = CardUpdateRequest.class),
//                            examples = @ExampleObject(
//                                    value = "{\"cardNumber\": \"4532015112830366\", \"ownerName\": \"JOHN DOE UPDATED\", \"expirationDate\": \"2028-12\"}"
//                            )
//                    )
//            ),
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Карта успешно обновлена",
//                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
//                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<CardResponse> updateCard(
//            @Parameter(description = "ID карты", example = "1", required = true)
//            @PathVariable Long id,
//            @Valid @RequestBody CardUpdateRequest cardUpdateRequest) {
//        log.info("PUT /api/admin/cards/{} - Обновление карты администратором", id);
//        CardResponse updatedCard = cardService.updateCard(id, cardUpdateRequest);
//        return ResponseEntity.ok(updatedCard);
//    }
//
//    @DeleteMapping("/admin/cards/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Удалить карту (Админ)",
//            description = "Удаляет карту по ID",
//            responses = {
//                    @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<Void> deleteCard(
//            @Parameter(description = "ID карты для удаления", example = "1", required = true)
//            @PathVariable Long id) {
//        log.info("DELETE /api/admin/cards/{} - Удаление карты администратором", id);
//        cardService.deleteCard(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/user/cards")
//    @PreAuthorize("hasRole('USER')")
//    @Operation(
//            summary = "Просмотреть мои карты (только юзер)",
//            description = "Возвращает список карт текущего аутентифицированного пользователя с пагинацией",
//            parameters = {
//                    @Parameter(name = "page", description = "Какую страницу показать (0 - первую страницу)"),
//                    @Parameter(name = "size", description = "Сколько карт поместить на странице"),
//                    @Parameter(name = "sort", description = "Поле для сортировки (например, по 'balance'). Пустое поле - сортировка по умолчанию")
//            },
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Список карт получен",
//                            content = @Content(schema = @Schema(implementation = org.springframework.data.domain.Page.class))),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
//                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
//            }
//    )
//    public ResponseEntity<Page<CardResponse>> getMyCards(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String sort) {
//
//        // Получаем ID текущего аутентифицированного пользователя
//        Long currentUserId = getCurrentUserId();
//        log.info("GET /api/user/cards - Запрос карт текущего пользователя ID: {}, page: {}, size: {}", currentUserId, page, size);
//
//        Pageable pageable;
//        if (sort != null && !sort.isEmpty()) {
//            pageable = PageRequest.of(page, size, Sort.by(sort));
//        } else {
//            pageable = PageRequest.of(page, size);
//        }
//
//        Page<CardResponse> cards = cardService.getUserCards(currentUserId, pageable);
//        return ResponseEntity.ok(cards);
//    }
//
//    // Вспомогательный метод для получения ID текущего пользователя
//    private Long getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || !authentication.isAuthenticated()) {
//            log.error("Попытка доступа без аутентификации");
//            throw new AuthenticationException("Пользователь не аутентифицирован");
//        }
//
//        // Получаем UserDetails из аутентификации
//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//
//        // Находим пользователя в базе по username чтобы получить ID
//        User user = userRepository.findByUsername(userDetails.getUsername())
//                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
//
//        return user.getId();
//    }
//
//    @PostMapping("/admin/cards/{id}/block")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Заблокировать карту (Админ)",
//            description = "Блокирует карту по ID",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Карта заблокирована",
//                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<CardResponse> blockMyCard(
//            @Parameter(description = "ID карты для блокировки", example = "1", required = true)
//            @PathVariable Long id) {
//        log.info("POST /api/admin/cards/{}/block - Блокировка карты администратором", id);
//        CardResponse blockedCard = cardService.blockCard(id);
//        return ResponseEntity.ok(blockedCard);
//    }
//
//
//    @PostMapping("/admin/cards/{id}/activate")
//    @PreAuthorize("hasRole('ADMIN')")
//    @Operation(
//            summary = "Активировать карту (Админ)",
//            description = "Активирует заблокированную карту по ID",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Карта активирована",
//                            content = @Content(schema = @Schema(implementation = CardResponse.class))),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
//            }
//    )
//    public ResponseEntity<CardResponse> activateMyCard(
//            @Parameter(description = "ID карты для активации", example = "1", required = true)
//            @PathVariable Long id) {
//        log.info("POST /api/admin/cards/{}/activate - Активация карты администратором", id);
//        CardResponse activatedCard = cardService.activateCard(id);
//        return ResponseEntity.ok(activatedCard);
//    }
//
//    // Новый эндпоинт для запроса блокировки карты пользователем
//    @PostMapping("/user/cards/{id}/block")
//    @PreAuthorize("hasRole('USER')")
//    @Operation(
//            summary = "Запросить блокировку своей карты (только юзер)",
//            description = "Аутентифицированный пользователь ТОЛЬКО запрашивает блокировку одной из своих карт по ID (но не блокирует её)",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Запрос на блокировку карты успешно сформирован",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = String.class),
//                                    examples = @ExampleObject(
//                                            value = "{\"message\": \"Пользователь JOHN DOE (id=2) отправил запрос на блокировку карты номер **** **** **** 0366 (id=1)\"}"
//                                    )
//                            )),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен или карта не принадлежит пользователю"),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
//            }
//    )
//    public ResponseEntity<String> blockUserCard(
//            @Parameter(description = "ID карты для блокировки", example = "1", required = true)
//            @PathVariable Long id) {
//        // Получаем ID текущего пользователя
//        Long currentUserId = getCurrentUserId();
//        log.info("POST /api/user/cards/{}/block - Запрос блокировки карты пользователем ID: {}", id, currentUserId);
//
//        // Получаем данные карты с проверкой принадлежности
//        CardResponse card = cardService.getCardById(id);
//
//        // Формируем ответное сообщение с уже замаскированным номером карты
//        String responseMessage = String.format(
//                "Пользователь %s (id=%d) отправил запрос на блокировку карты номер %s (id=%d)",
//                card.getOwnerName(), currentUserId, card.getMaskedCardNumber(), id);
//
//        // Возвращаю JSON просто с сообщением (для теста функционала)
//        return ResponseEntity.ok("{\"message\": \"" + responseMessage + "\"}");
//    }
//
//    // добавленный код: Новый эндпоинт для получения баланса карты пользователем
//    @GetMapping("/user/cards/{id}/balance")
//    @PreAuthorize("hasRole('USER')")
//    @Operation(
//            summary = "Просмотреть баланс своей карты (только юзер)",
//            description = "Возвращает баланс указанной карты, принадлежащей текущему аутентифицированному пользователю",
//            // изменил ИИ: Удалены параметры из @Operation, так как @Parameter указан в аргументе метода
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Баланс карты успешно получен",
//                            content = @Content(
//                                    mediaType = "application/json",
//                                    schema = @Schema(implementation = Map.class),
//                                    examples = @ExampleObject(
//                                            value = "{\"balance\": 1000.5}"
//                                    )
//                            )),
//                    @ApiResponse(responseCode = "403", description = "Доступ запрещен или карта не принадлежит пользователю"),
//                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
//                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
//            }
//    )
//    public ResponseEntity<Map<String, Double>> getCardBalance(
//            // добавленный код: Аннотация для ID карты
//            @Parameter(description = "ID карты для просмотра баланса", example = "1", required = true)
//            @PathVariable Long id) {
//        // добавленный код: Логирование запроса
//        Long currentUserId = getCurrentUserId();
//        log.info("GET /api/user/cards/{}/balance - Запрос баланса карты пользователем ID: {}", id, currentUserId);
//
//        // добавленный код: Получаем данные карты с проверкой принадлежности
//        CardResponse card = cardService.getCardById(id);
//
//        // добавленный код: Формируем JSON-ответ с полем balance
//        Map<String, Double> response = Map.of("balance", card.getBalance());
//
//        // добавленный код: Возвращаем JSON-ответ
//        return ResponseEntity.ok(response);
//    }
//
//}