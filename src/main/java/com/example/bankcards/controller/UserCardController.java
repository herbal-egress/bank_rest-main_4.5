package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AuthenticationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransactionService;
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

import java.util.Map;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@SecurityScheme(
        name = "bearerAuth",
        type = HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT токен для авторизации. Вставьте: Bearer <токен>"
)


@RestController
@RequestMapping("/api/user")
@Tag(name = "Операции с картами (Юзер)", description = "Для аутентифицированного пользователя с ролью USER")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class UserCardController {

    private final CardService cardService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @GetMapping("/cards")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Просмотреть свои карты (только юзер)",
            description = "Возвращает список карт текущего аутентифицированного пользователя с пагинацией",
            parameters = {
                    @Parameter(name = "page", description = "Какую страницу показать (0 - первую страницу)"),
                    @Parameter(name = "size", description = "Сколько карт поместить на странице"),
                    @Parameter(name = "sort", description = "Поле для сортировки (например, по 'balance'). Пустое поле - сортировка по умолчанию")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карт получен",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            }
    )
    public ResponseEntity<Page<CardResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        
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

    @PostMapping("/cards/{id}/block")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запросить блокировку своей карты (только юзер)",
            description = "Аутентифицированный пользователь запрашивает блокировку одной из своих карт по ID (без фактической блокировки)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Запрос на блокировку карты успешно сформирован",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = String.class),
                                    examples = @ExampleObject(
                                            value = "{\"message\": \"Пользователь JOHN DOE (id=2) отправил запрос на блокировку карты номер **** **** **** 0366 (id=1)\"}"
                                    )
                            )),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен или карта не принадлежит пользователю"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            }
    )
    public ResponseEntity<String> blockUserCard(
            @Parameter(description = "ID карты для блокировки", example = "1", required = true)
            @PathVariable Long id) {
        
        Long currentUserId = getCurrentUserId();
        log.info("POST /api/user/cards/{}/block - Запрос блокировки карты пользователем ID: {}", id, currentUserId);

        
        CardResponse card = cardService.getCardById(id);

        
        String responseMessage = String.format(
                "Пользователь %s (id=%d) отправил запрос на блокировку карты номер %s (id=%d)",
                card.getOwnerName(), currentUserId, card.getMaskedCardNumber(), id);

        
        return ResponseEntity.ok("{\"message\": \"" + responseMessage + "\"}");
    }

    @GetMapping("/cards/{id}/balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Просмотреть баланс своей карты (только юзер)",
            description = "Возвращает баланс указанной карты, принадлежащей текущему аутентифицированному пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Баланс карты успешно получен",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            value = "{\"balance\": 1000.5}"
                                    )
                            )),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен или карта не принадлежит пользователю"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
            }
    )
    public ResponseEntity<Map<String, Double>> getCardBalance(
            @Parameter(description = "ID карты для просмотра баланса", example = "1", required = true)
            @PathVariable Long id) {
        
        Long currentUserId = getCurrentUserId();
        log.info("GET /api/user/cards/{}/balance - Запрос баланса карты пользователем ID: {}", id, currentUserId);

        
        CardResponse card = cardService.getCardById(id);

        
        Map<String, Double> response = Map.of("balance", card.getBalance());

        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/transfer")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Выполнить перевод между своими картами (только юзер)",
            description = "Позволяет пользователю перевести средства между своими картами",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Перевод выполнен",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Неверные данные"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Карта не найдена")
            }
    )
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("POST /api/user/transactions/transfer - Запрос на перевод");
        
        TransactionResponse response = transactionService.transfer(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Попытка доступа без аутентификации");
            throw new AuthenticationException("Пользователь не аутентифицирован");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        return user.getId();
    }
}