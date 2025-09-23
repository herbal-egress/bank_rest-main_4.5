package com.example.bankcards.service;

// добавленный код: Импорты необходимых классов, аннотаций и исключений
import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// добавленный код: Кастомное исключение для недостатка средств (будет создано ниже)
import com.example.bankcards.exception.InsufficientFundsException;
// добавленный код: Кастомное исключение для перевода на ту же карту
import com.example.bankcards.exception.SameCardTransferException;

// добавленный код: Сервис для обработки транзакций
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    // добавленный код: Метод для выполнения перевода
    @Transactional
    public TransactionResponse transfer(TransactionRequest request) {
        log.info("Запрос на перевод: с карты {} на карту {}, сумма {}", request.getFromCardId(), request.getToCardId(), request.getAmount());

        // добавленный код: Получаем текущего пользователя
        Long currentUserId = getCurrentUserId();

        // добавленный код: Находим карты
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта-отправитель с ID " + request.getFromCardId() + " не найдена"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта-получатель с ID " + request.getToCardId() + " не найдена"));

        // добавленный код: Проверяем принадлежность карт текущему пользователю
        if (!fromCard.getUser().getId().equals(currentUserId) || !toCard.getUser().getId().equals(currentUserId)) {
            log.error("Попытка перевода между чужими картами для пользователя ID: {}", currentUserId);
            throw new InvalidCardOperationException("Перевод возможен только между своими картами");
        }

        // добавленный код: Проверяем, что карты разные
        if (fromCard.getId().equals(toCard.getId())) {
            log.warn("Попытка перевода на ту же карту ID: {}", fromCard.getId());
            throw new SameCardTransferException("Перевод на ту же карту запрещен");
        }

        // добавленный код: Проверяем статус карт
        if (fromCard.getStatus() != Card.Status.ACTIVE || toCard.getStatus() != Card.Status.ACTIVE) {
            log.warn("Попытка перевода с/на неактивную карту");
            throw new InvalidCardOperationException("Обе карты должны быть активны");
        }

        // добавленный код: Проверяем баланс
        if (fromCard.getBalance() < request.getAmount()) {
            log.warn("Недостаточно средств на карте ID: {}", fromCard.getId());
            throw new InsufficientFundsException("Недостаточно средств на карте-отправителе");
        }

        // добавленный код: Обновляем балансы
        fromCard.setBalance(fromCard.getBalance() - request.getAmount());
        toCard.setBalance(toCard.getBalance() + request.getAmount());

        // добавленный код: Сохраняем карты
        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // добавленный код: Создаем транзакцию
        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(Transaction.Status.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Перевод успешно выполнен, ID транзакции: {}", savedTransaction.getId());

        // добавленный код: Маппинг в response
        return mapToResponse(savedTransaction);
    }

    // добавленный код: Вспомогательный метод для маппинга
    private TransactionResponse mapToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setFromCardId(transaction.getFromCard().getId());
        response.setToCardId(transaction.getToCard().getId());
        response.setAmount(transaction.getAmount());
        response.setTimestamp(transaction.getTimestamp());
        response.setStatus(transaction.getStatus().name());
        return response;
    }

    // добавленный код: Вспомогательный метод для получения ID текущего пользователя (аналогично CardService)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Попытка доступа без аутентификации");
            throw new com.example.bankcards.exception.AuthenticationException("Пользователь не аутентифицирован");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new com.example.bankcards.exception.UserNotFoundException("Пользователь не найден"));
        return user.getId();
    }
}