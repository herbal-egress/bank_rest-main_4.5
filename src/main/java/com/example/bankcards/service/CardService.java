// service/CardService.java
package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberAlreadyExistsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.card.CardUpdateRequest; // Добавленный импорт
// Добавленный код: Импорт для работы с аутентификацией
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Добавленный код: Сервисный слой для бизнес-логики работы с картами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService; // Будет создан ниже

    /**
     * Добавленный код: Создание новой карты.
     * Шифрует номер карты, проверяет уникальность, устанавливает статус.
     */
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        log.info("Запрос на создание карты для пользователя ID: {}", cardRequest.getUserId());

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + cardRequest.getUserId() + " не найден"));

        // Шифруем номер карты
        String encryptedCardNumber = encryptionService.encrypt(cardRequest.getCardNumber());

        // Проверка на существующую карту с таким номером
        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            log.error("Попытка создать карту с уже существующим номером: {}", cardRequest.getCardNumber());
            throw new CardNumberAlreadyExistsException("Карта с номером '" + cardRequest.getCardNumber() + "' уже существует");
        }

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setOwnerName(cardRequest.getOwnerName());
        card.setExpirationDate(cardRequest.getExpirationDate());
        card.setBalance(cardRequest.getBalance() != null ? cardRequest.getBalance() : 0.0);
        card.setUser(user);

        // Проверяем срок действия и устанавливаем статус
        card.setStatus(determineCardStatus(cardRequest.getExpirationDate()));

        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {} для пользователя: {}", savedCard.getId(), user.getUsername());

        return mapToCardResponse(savedCard);
    }

    // Добавленный код: Получение карты по ID.
    // Добавленный код: В сервисе оставляем метод без изменений, но добавляем проверку принадлежности карты
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        log.debug("Запрос карты по ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        // Добавленный код: Проверяем, принадлежит ли карта текущему пользователю (для USER)
        checkCardOwnership(card);

        return mapToCardResponse(card);
    }

    // Добавленный код: Метод проверки принадлежности карты текущему пользователю
    private void checkCardOwnership(Card card) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Проверяем, является ли пользователь ADMIN или владельцем карты
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !card.getUser().getUsername().equals(userDetails.getUsername())) {
                log.error("Попытка доступа к чужой карте. Пользователь: {}, Владелец карты: {}",
                        userDetails.getUsername(), card.getUser().getUsername());
                throw new AccessDeniedException("Доступ к карте запрещен");
            }
        }
    }

    // Получение всех карт пользователя с пагинацией.
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        log.debug("Запрос карт пользователя ID: {} с пагинацией", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Page<Card> cards = cardRepository.findByUserId(userId, pageable);
        log.info("Найдено {} карт для пользователя ID: {}", cards.getTotalElements(), userId);

        return cards.map(this::mapToCardResponse);
    }

    /**
     * Добавленный код: Получение всех карт (для администратора).
     */
    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        log.debug("Запрос всех карт");
        List<Card> cards = cardRepository.findAll();
        log.info("Найдено {} карт в системе", cards.size());
        return cards.stream()
                .map(this::mapToCardResponse)
                .collect(Collectors.toList());
    }

    /**
     * Добавленный код: Обновление данных карты.
     */
    @Transactional
    public CardResponse updateCard(Long id, CardUpdateRequest cardUpdateRequest) {
        log.info("Запрос на обновление карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        // Обновляем номер карты только если он предоставлен в запросе
        if (cardUpdateRequest.getCardNumber() != null && !cardUpdateRequest.getCardNumber().trim().isEmpty()) {
            String encryptedCardNumber = encryptionService.encrypt(cardUpdateRequest.getCardNumber());

            // Проверяем, не занят ли новый номер другой картой
            if (!card.getEncryptedCardNumber().equals(encryptedCardNumber) &&
                    cardRepository.existsByEncryptedCardNumberAndIdNot(encryptedCardNumber, id)) {
                log.error("Попытка обновить номер карты на уже занятый: {}", cardUpdateRequest.getCardNumber());
                throw new CardNumberAlreadyExistsException("Номер карты '" + cardUpdateRequest.getCardNumber() + "' уже занят");
            }

            card.setEncryptedCardNumber(encryptedCardNumber);
        }

        // Обновляем имя владельца только если оно предоставлено в запросе
        if (cardUpdateRequest.getOwnerName() != null) {
            card.setOwnerName(cardUpdateRequest.getOwnerName());
        }

        // Обновляем срок действия только если он предоставлен в запросе
        if (cardUpdateRequest.getExpirationDate() != null) {
            card.setExpirationDate(cardUpdateRequest.getExpirationDate());
            // Обновляем статус на основе нового срока действия
            card.setStatus(determineCardStatus(cardUpdateRequest.getExpirationDate()));
        }

        Card updatedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно обновлена", id);
        return mapToCardResponse(updatedCard);
    }

    /**
     * Добавленный код: Блокировка карты.
     */
    @Transactional
    public CardResponse blockCard(Long id) {
        log.info("Запрос на блокировку карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        if (card.getStatus() == Card.Status.BLOCKED) {
            log.warn("Попытка заблокировать уже заблокированную карту ID: {}", id);
            throw new InvalidCardOperationException("Карта уже заблокирована");
        }

        if (card.getStatus() == Card.Status.EXPIRED) {
            log.warn("Попытка заблокировать карту с истекшим сроком ID: {}", id);
            throw new InvalidCardOperationException("Нельзя заблокировать карту с истекшим сроком действия");
        }

        card.setStatus(Card.Status.BLOCKED);
        Card blockedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно заблокирована", id);
        return mapToCardResponse(blockedCard);
    }

    /**
     * Добавленный код: Активация карты.
     */
    @Transactional
    public CardResponse activateCard(Long id) {
        log.info("Запрос на активацию карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        if (card.getStatus() == Card.Status.ACTIVE) {
            log.warn("Попытка активировать уже активную карту ID: {}", id);
            throw new InvalidCardOperationException("Карта уже активна");
        }

        if (card.getStatus() == Card.Status.EXPIRED) {
            log.warn("Попытка активировать карту с истекшим сроком ID: {}", id);
            throw new InvalidCardOperationException("Нельзя активировать карту с истекшим сроком действия");
        }

        card.setStatus(Card.Status.ACTIVE);
        Card activatedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно активирована", id);
        return mapToCardResponse(activatedCard);
    }

    /**
     * Добавленный код: Удаление карты по ID.
     */
    @Transactional
    public void deleteCard(Long id) {
        log.info("Запрос на удаление карты с ID: {}", id);
        if (!cardRepository.existsById(id)) {
            log.error("Попытка удалить несуществующую карту с ID: {}", id);
            throw new CardNotFoundException("Карта с ID " + id + " не найдена");
        }
        cardRepository.deleteById(id);
        log.info("Карта с ID {} успешно удалена", id);
    }

    /**
     * Добавленный код: Определяет статус карты на основе срока действия.
     */
    private Card.Status determineCardStatus(YearMonth expirationDate) {
        YearMonth current = YearMonth.now();
        if (expirationDate.isBefore(current)) {
            return Card.Status.EXPIRED;
        }
        return Card.Status.ACTIVE;
    }

    /**
     * Добавленный код: Преобразует Card в CardResponse с маскированным номером.
     */
    private CardResponse mapToCardResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setMaskedCardNumber(CardResponse.maskCardNumber(card.getEncryptedCardNumber()));
        response.setOwnerName(card.getOwnerName());
        response.setExpirationDate(card.getExpirationDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setUserId(card.getUser().getId());
        return response;
    }
}