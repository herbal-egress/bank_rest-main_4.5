// controller/CardController.java
package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * Добавленный код: REST контроллер для операций с картами.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Карты", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth") // Применяет схему безопасности ко всем методам, указывает требование токена в Swagger
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final CardService cardService;

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
    @Operation(summary = "Обновить карту (Админ)", description = "Обновляет данные карты")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id, @Valid @RequestBody CardRequest cardRequest) {
        log.info("PUT /api/admin/cards/{} - Обновление карты администратором", id);
        CardResponse updatedCard = cardService.updateCard(id, cardRequest);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/admin/cards/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить карту (Админ)", description = "Удаляет карту по идентификатору")
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
        // В реальном приложении userId брался бы из аутентификации
        Page<CardResponse> cards = cardService.getUserCards(userId, pageable);
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
}