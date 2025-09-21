// controller/CardController.java
package com.example.bankcards.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import com.example.bankcards.exception.AuthenticationException; // Если нет, создать
import com.example.bankcards.service.UserService;
import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для операций с картами.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Карты", description = "Управление банковскими картами")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    // Административные endpoints
    @PostMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать карту (Админ)", description = "Создает новую банковскую карту")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest cardRequest) {
        log.info("POST /api/admin/cards - Создание карты администратором");
        CardResponse createdCard = cardService.createCard(cardRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/admin/cards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить все карты (Админ)", description = "Возвращает список всех карт в системе")
    public ResponseEntity<List<CardResponse>> getAllCards() {
        log.info("GET /api/admin/cards - Запрос всех карт администратором");
        List<CardResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить карту (Админ)", description = "Обновляет данные существующей карты")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id,
                                                   @Valid @RequestBody CardRequest cardRequest) {
        log.info("PUT /api/admin/cards/{} - Обновление карты администратором", id);
        CardResponse updatedCard = cardService.updateCard(id, cardRequest);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту (Админ)", description = "Удаляет карту из системы")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        log.info("DELETE /api/admin/cards/{} - Удаление карты администратором", id);
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    // Пользовательские endpoints
    @GetMapping("/user/cards")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить мои карты", description = "Возвращает список карт текущего пользователя с пагинацией")
    public ResponseEntity<Page<CardResponse>> getMyCards(Pageable pageable,
                                                         @RequestParam(required = false) Long userId) {
        log.info("GET /api/user/cards - Запрос карт пользователя");

        // Получаем текущего аутентифицированного пользователя из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Пользователь не аутентифицирован при запросе карт");
            throw new AuthenticationException("Пользователь не аутентифицирован");
        }

        String currentUsername = authentication.getName();
        log.debug("Текущий аутентифицированный пользователь: {}", currentUsername);

        // Получаем ID текущего пользователя из UserService
        Long currentUserId;
        try {
            currentUserId = userService.getUserByUsername(currentUsername).getId();
            log.debug("ID текущего пользователя: {}", currentUserId);
        } catch (UserNotFoundException e) {
            log.error("Текущий пользователь {} не найден в базе данных", currentUsername);
            throw new AuthenticationException("Ошибка идентификации пользователя");
        }

        // Определяем, чьи карты запрашиваются
        Long actualUserId;
        if (userId != null) {
            // Если передан userId, проверяем права доступа
            if (!userId.equals(currentUserId)) {
                // Проверяем, является ли текущий пользователь администратором
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                if (!isAdmin) {
                    log.warn("Пользователь {} пытается получить карты другого пользователя (ID: {})",
                            currentUsername, userId);
                    throw new AccessDeniedException("Доступ к картам другого пользователя запрещен");
                }
                log.debug("Администратор {} запрашивает карты пользователя ID: {}", currentUsername, userId);
            }
            actualUserId = userId;
        } else {
            // Если userId не передан, показываем карты текущего пользователя
            actualUserId = currentUserId;
            log.debug("Показываются карты текущего пользователя ID: {}", actualUserId);
        }

        // Получаем карты с валидацией
        Page<CardResponse> cards = cardService.getUserCards(actualUserId, pageable);
        log.info("Успешно возвращено {} карт для пользователя ID: {} (страница: {}, размер: {})",
                cards.getTotalElements(), actualUserId, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/cards/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить карту по ID", description = "Возвращает данные конкретной карты пользователя")
    public ResponseEntity<CardResponse> getMyCardById(@PathVariable Long id) {
        log.info("GET /api/user/cards/{} - Запрос карты пользователя", id);
        CardResponse card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/user/cards/{id}/block")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Заблокировать карту", description = "Блокирует карту по запросу пользователя")
    public ResponseEntity<CardResponse> blockMyCard(@PathVariable Long id) {
        log.info("POST /api/user/cards/{}/block - Блокировка карты пользователем", id);
        CardResponse blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/user/cards/{id}/activate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Активировать карту", description = "Активирует заблокированную карту")
    public ResponseEntity<CardResponse> activateMyCard(@PathVariable Long id) {
        log.info("POST /api/user/cards/{}/activate - Активация карты пользователем", id);
        CardResponse activatedCard = cardService.activateCard(id);
        return ResponseEntity.ok(activatedCard);
    }

    // ДОПОЛНИТЕЛЬНЫЙ ENDPOINT: Получение активных карт пользователя
    @GetMapping("/user/cards/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Получить активные карты", description = "Возвращает только активные карты текущего пользователя")
    public ResponseEntity<List<CardResponse>> getMyActiveCards(@RequestParam(required = false) Long userId,
                                                               Authentication authentication) {
        log.info("GET /api/user/cards/active - Запрос активных карт пользователя");

        // Аналогично getMyCards - логика получения userId
        Long actualUserId = userId;
        if (userId == null) {
            log.warn("userId не передан для активных карт");
            // actualUserId = getCurrentUserId(authentication);
        }

        List<CardResponse> activeCards = cardService.getActiveCardsByUserId(actualUserId);
        return ResponseEntity.ok(activeCards);
    }
}