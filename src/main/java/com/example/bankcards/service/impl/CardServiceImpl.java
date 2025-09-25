package com.example.bankcards.service.impl;

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
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.EncryptionService;
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
import java.security.SecureRandom;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private static final String CARD_NUMBER_PREFIX = "3985";
    private static final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public CardResponse createCard(CardRequest cardRequest) {
        log.info("Запрос на создание карты для пользователя ID: {}", cardRequest.getUserId());

        if (cardRequest.getBalance() != null && cardRequest.getBalance() < 0) {
            log.error("Попытка создать карту с отрицательным балансом: {}", cardRequest.getBalance());
            throw new InvalidCardOperationException("Баланс карты не может быть отрицательным");
        }

        User user = userRepository.findById(cardRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь с ID " + cardRequest.getUserId() + " не найден"));

        String cardNumber = generateCardNumber();
        String encryptedCardNumber = encryptionService.encrypt(cardNumber);

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
        card.setStatus(determineCardStatus(cardRequest.getExpirationDate()));

        Card savedCard = cardRepository.save(card);
        log.info("Карта успешно создана с ID: {} для пользователя: {}", savedCard.getId(), user.getUsername());

        return mapToCardResponse(savedCard);
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_PREFIX);
        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public CardResponse getCardById(Long id) {
        log.debug("Запрос карты по ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        checkCardOwnership(card);
        return mapToCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getUserCards(Long userId, Pageable pageable) {
        log.debug("Запрос всех карт для пользователя ID: {} с пагинацией", userId);
        Page<Card> cards = cardRepository.findByUserId(userId, pageable);
        return cards.map(this::mapToCardResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getAllCards() {
        log.debug("Запрос всех карт без пагинации");
        List<Card> cards = cardRepository.findAll();
        return cards.stream().map(this::mapToCardResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long id, CardUpdateRequest cardUpdateRequest) {
        log.info("Запрос на обновление карты с ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта с ID " + id + " не найдена"));

        checkCardOwnership(card);

        if (cardUpdateRequest.getOwnerName() != null && !cardUpdateRequest.getOwnerName().isBlank()) {
            card.setOwnerName(cardUpdateRequest.getOwnerName());
        }
        if (cardUpdateRequest.getExpirationDate() != null) {
            card.setExpirationDate(cardUpdateRequest.getExpirationDate());
            card.setStatus(determineCardStatus(cardUpdateRequest.getExpirationDate()));
        }

        Card updatedCard = cardRepository.save(card);
        log.info("Карта с ID {} успешно обновлена", id);
        return mapToCardResponse(updatedCard);
    }

    @Override
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

    @Override
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

    @Override
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

    private Card.Status determineCardStatus(YearMonth expirationDate) {
        YearMonth current = YearMonth.now();
        if (expirationDate.isBefore(current)) {
            return Card.Status.EXPIRED;
        }
        return Card.Status.ACTIVE;
    }

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