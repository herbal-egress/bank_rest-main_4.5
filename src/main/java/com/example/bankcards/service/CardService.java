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
import com.example.bankcards.exception.NegativeBalanceException; // Добавленный кастомный exception
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

// Сервисный слой для бизнес-логики работы с картами.
@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    // Создание новой карты. Шифрует номер карты, проверяет уникальность, устанавливает статус.
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        log.info("Запрос на создание карты для пользователя ID: {}", cardRequest.getUserId());

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", cardRequest.getUserId());
                    return new UserNotFoundException("Пользователь с ID " + cardRequest.getUserId() + " не найден");
                });

        // Шифруем номер карты
        String encryptedCardNumber = encryptionService.encrypt(cardRequest.getCardNumber());

        // Проверка на существующую карту с таким номером
        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            log.error("Попытка создать карту с уже существующим номером: {}", cardRequest.getCardNumber().substring(0, 4) + "****");
            throw new CardNumberAlreadyExistsException("Карта с номером '" + cardRequest.getCardNumber() + "' уже существует");
        }

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setOwnerName(cardRequest.getOwnerName());
        card.setExpirationDate(cardRequest.getExpirationDate());
        card.setStatus(determineCardStatus(cardRequest.getExpirationDate()));
        card.setBalance(cardRequest.getBalance() != null ? cardRequest.getBalance() : 0.0);
        if (card.getBalance() < 0) {
            log.error("Попытка создать карту с отрицательным балансом: {}", card.getBalance());
            throw new NegativeBalanceException("Баланс карты не может быть отрицательным");
        }
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {}", savedCard.getId());
        return mapToCardResponse(savedCard);
    }

    // Получение карты по ID.
    public CardResponse getCardById(Long id) {
        log.debug("Запрос на получение карты по ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена", id);
                    return new CardNotFoundException("Карта с ID " + id + " не найдена");
                });
        log.info("Карта с ID {} успешно получена", id);
        return mapToCardResponse(card);
    }

    // НЕДОСТАЮЩИЙ МЕТОД: Получение карт пользователя с пагинацией (для пользовательского endpoint)
    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        log.debug("Запрос на получение карт пользователя ID: {} с пагинацией", userId);

        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с ID {} не найден для получения карт", userId);
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Page<Card> cards = cardRepository.findByUserId(userId, pageable);
        log.info("Получено {} карт для пользователя ID: {} (страница: {}, размер: {})",
                cards.getTotalElements(), userId, pageable.getPageNumber(), pageable.getPageSize());
        return cards.map(this::mapToCardResponse);
    }

    // Получение активных карт пользователя.
    public List<CardResponse> getActiveCardsByUserId(Long userId) {
        log.debug("Запрос на получение активных карт пользователя ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.error("Пользователь с ID {} не найден для получения активных карт", userId);
            throw new UserNotFoundException("Пользователь с ID " + userId + " не найден");
        }
        List<Card> activeCards = cardRepository.findActiveCardsByUserId(userId);
        log.info("Получено {} активных карт для пользователя ID: {}", activeCards.size(), userId);
        return activeCards.stream().map(this::mapToCardResponse).collect(Collectors.toList());
    }

    // НЕДОСТАЮЩИЙ МЕТОД: Получение всех карт (для административного endpoint)
    public List<CardResponse> getAllCards() {
        log.debug("Запрос на получение всех карт в системе");
        List<Card> allCards = cardRepository.findAll();
        if (allCards.isEmpty()) {
            log.warn("Список всех карт пуст");
        } else {
            log.info("Получено {} карт из системы", allCards.size());
        }
        return allCards.stream().map(this::mapToCardResponse).collect(Collectors.toList());
    }

    // Обновление карты.
    @Transactional
    public CardResponse updateCard(Long id, CardRequest cardRequest) {
        log.info("Запрос на обновление карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена для обновления", id);
                    return new CardNotFoundException("Карта с ID " + id + " не найдена");
                });

        // Проверяем, что обновляется карта принадлежащая указанному пользователю
        if (cardRequest.getUserId() != null && !cardRequest.getUserId().equals(card.getUser().getId())) {
            log.error("Попытка обновить карту другого пользователя (текущий: {}, запрошенный: {})",
                    card.getUser().getId(), cardRequest.getUserId());
            throw new InvalidCardOperationException("Нельзя обновить карту другого пользователя");
        }

        // Обновляем номер карты, если изменился
        if (cardRequest.getCardNumber() != null) {
            String newEncryptedNumber = encryptionService.encrypt(cardRequest.getCardNumber());
            if (!newEncryptedNumber.equals(card.getEncryptedCardNumber()) &&
                    cardRepository.existsByEncryptedCardNumberAndIdNot(newEncryptedNumber, id)) {
                log.error("Попытка обновить номер карты на уже существующий");
                throw new CardNumberAlreadyExistsException("Карта с номером '" + cardRequest.getCardNumber() + "' уже существует");
            }
            card.setEncryptedCardNumber(newEncryptedNumber);
        }

        if (cardRequest.getOwnerName() != null) {
            card.setOwnerName(cardRequest.getOwnerName());
        }
        if (cardRequest.getExpirationDate() != null) {
            card.setExpirationDate(cardRequest.getExpirationDate());
            card.setStatus(determineCardStatus(cardRequest.getExpirationDate()));
        }
        if (cardRequest.getBalance() != null) {
            if (cardRequest.getBalance() < 0) {
                log.error("Попытка обновить баланс на отрицательный: {}", cardRequest.getBalance());
                throw new NegativeBalanceException("Баланс карты не может быть отрицательным");
            }
            card.setBalance(cardRequest.getBalance());
        }

        Card updatedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно обновлена", id);
        return mapToCardResponse(updatedCard);
    }

    // Блокировка карты.
    @Transactional
    public CardResponse blockCard(Long id) {
        log.info("Запрос на блокировку карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена для блокировки", id);
                    return new CardNotFoundException("Карта с ID " + id + " не найдена");
                });

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

    // Активация карты.
    @Transactional
    public CardResponse activateCard(Long id) {
        log.info("Запрос на активацию карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Карта с ID {} не найдена для активации", id);
                    return new CardNotFoundException("Карта с ID " + id + " не найдена");
                });

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

    // Удаление карты по ID.
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

    // Определяет статус карты на основе срока действия.
    private Card.Status determineCardStatus(YearMonth expirationDate) {
        YearMonth current = YearMonth.now();
        if (expirationDate.isBefore(current)) {
            log.debug("Статус карты: EXPIRED, срок истек: {}", expirationDate);
            return Card.Status.EXPIRED;
        }
        log.debug("Статус карты: ACTIVE, срок: {}", expirationDate);
        return Card.Status.ACTIVE;
    }

    // Преобразует Card в CardResponse с маскированным номером.
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