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
import com.example.bankcards.dto.card.CardUpdateRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.SecureRandom; // добавленный код: Импорт для генерации безопасных случайных чисел
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

// добавленный код: Сервисный слой для бизнес-логики работы с картами.
@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    // добавленный код: Константа для начальных цифр номера карты
    private static final String CARD_NUMBER_PREFIX = "3985";
    // добавленный код: Объект SecureRandom для криптографически безопасной генерации
    private static final SecureRandom random = new SecureRandom();

    // добавленный код: Создание новой карты.
    // изменил ИИ: Добавлена генерация номера карты с префиксом "3985" и случайными 12 цифрами.
    // изменил ИИ: Удалена зависимость от cardNumber в CardRequest, так как он теперь генерируется.
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        log.info("Запрос на создание карты для пользователя ID: {}", cardRequest.getUserId());

        // изменил ИИ: Проверка баланса на неотрицательность
        if (cardRequest.getBalance() != null && cardRequest.getBalance() < 0) {
            log.error("Попытка создать карту с отрицательным балансом: {}", cardRequest.getBalance());
            throw new InvalidCardOperationException("Баланс карты не может быть отрицательным");
        }

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + cardRequest.getUserId() + " не найден"));

        // добавленный код: Генерация 16-значного номера карты с префиксом "3985"
        String cardNumber = generateCardNumber();
        // добавленный код: Шифруем номер карты
        String encryptedCardNumber = encryptionService.encrypt(cardNumber);

        // добавленный код: Проверка на существующую карту с таким номером
        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            log.error("Попытка создать карту с уже существующим номером: {}", cardNumber);
            throw new CardNumberAlreadyExistsException("Карта с номером '" + cardNumber + "' уже существует");
        }

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setOwnerName(cardRequest.getOwnerName());
        card.setExpirationDate(cardRequest.getExpirationDate());
        card.setBalance(cardRequest.getBalance() != null ? cardRequest.getBalance() : 0.0);
        card.setUser(user);

        // добавленный код: Проверяем срок действия и устанавливаем статус
        card.setStatus(determineCardStatus(cardRequest.getExpirationDate()));

        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {} для пользователя: {}", savedCard.getId(), user.getUsername());

        return mapToCardResponse(savedCard);
    }

    // добавленный код: Метод для генерации 16-значного номера карты
    private String generateCardNumber() {
        // добавленный код: Создаем StringBuilder для построения номера карты
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_PREFIX);
        // добавленный код: Генерируем 12 случайных цифр
        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }
        // добавленный код: Возвращаем сгенерированный номер карты
        return cardNumber.toString();
    }

    // добавленный код: Получение карты по ID.
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        log.debug("Запрос карты по ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        // добавленный код: Проверяем, принадлежит ли карта текущему пользователю (для USER)
        checkCardOwnership(card);

        return mapToCardResponse(card);
    }

    // изменил ИИ: Изменена сигнатура метода: добавлен Pageable для поддержки пагинации, возврат изменен на Page<CardResponse> для соответствия контроллеру getMyCards.
    // изменил ИИ: Внутренняя реализация изменена на использование cardRepository.findByUserId(userId, pageable) для пагинации.
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        log.debug("Запрос всех карт для пользователя ID: {} с пагинацией", userId);
        Page<Card> cards = cardRepository.findByUserId(userId, pageable);
        return cards.map(this::mapToCardResponse);
    }

    // изменил ИИ: Изменена сигнатура метода: удален Pageable, возврат изменен на List<CardResponse> для соответствия контроллеру getAllCards без пагинации.
    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        log.debug("Запрос всех карт без пагинации");
        List<Card> cards = cardRepository.findAll();
        return cards.stream().map(this::mapToCardResponse).collect(Collectors.toList());
    }

    // добавленный код: Обновление карты.
    @Transactional
    public CardResponse updateCard(Long id, CardUpdateRequest cardUpdateRequest) {
        log.info("Запрос на обновление карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        // добавленный код: Проверяем, принадлежит ли карта текущему пользователю (для USER)
        checkCardOwnership(card);

        // добавленный код: Обновляем поля, если они предоставлены
        if (cardUpdateRequest.getOwnerName() != null && !cardUpdateRequest.getOwnerName().isBlank()) {
            card.setOwnerName(cardUpdateRequest.getOwnerName());
        }
        if (cardUpdateRequest.getExpirationDate() != null) {
            card.setExpirationDate(cardUpdateRequest.getExpirationDate());
            // добавленный код: Обновляем статус на основе нового срока действия
            card.setStatus(determineCardStatus(cardUpdateRequest.getExpirationDate()));
        }

        Card updatedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно обновлена", id);
        return mapToCardResponse(updatedCard);
    }

    // добавленный код: Блокировка карты.
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

    // добавленный код: Активация карты.
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

    // добавленный код: Удаление карты по ID.
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

    // добавленный код: Определяет статус карты на основе срока действия.
    private Card.Status determineCardStatus(YearMonth expirationDate) {
        YearMonth current = YearMonth.now();
        if (expirationDate.isBefore(current)) {
            return Card.Status.EXPIRED;
        }
        return Card.Status.ACTIVE;
    }

    // добавленный код: Проверяет принадлежность карты текущему пользователю.
    private void checkCardOwnership(Card card) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                User currentUser = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
                if (!card.getUser().getId().equals(currentUser.getId())) {
                    log.warn("Попытка доступа к чужой карте ID: {} пользователем: {}", card.getId(), currentUser.getUsername());
                    throw new AccessDeniedException("Доступ к карте запрещен");
                }
            }
        }
    }

    // добавленный код: Преобразует Card в CardResponse с маскированным номером.
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