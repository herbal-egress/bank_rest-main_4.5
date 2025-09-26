package com.example.bankcards.service.impl;

import com.example.bankcards.dto.transaction.TransactionRequest;
import com.example.bankcards.dto.transaction.TransactionResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.exception.SameCardTransferException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionRequest request) {
        log.info("Запрос на перевод: с карты {} на карту {}, сумма {}", request.getFromCardId(), request.getToCardId(), request.getAmount());

        Long currentUserId = getCurrentUserId();

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта-отправитель с ID " + request.getFromCardId() + " не найдена"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта-получатель с ID " + request.getToCardId() + " не найдена"));

        if (fromCard.getId().equals(toCard.getId())) {
            log.error("Попытка перевода на ту же карту: {}", fromCard.getId());
            throw new SameCardTransferException("Нельзя выполнить перевод на ту же карту");
        }

        if (!fromCard.getUser().getId().equals(currentUserId)) {
            log.error("Попытка перевода с чужой карты: {} пользователем: {}", fromCard.getId(), currentUserId);
            throw new InvalidCardOperationException("Вы не можете использовать эту карту для перевода");
        }

        if (fromCard.getStatus() != Card.Status.ACTIVE) {
            log.error("Карта-отправитель ID {} не активна: {}", fromCard.getId(), fromCard.getStatus());
            throw new InvalidCardOperationException("Карта-отправитель не активна");
        }

        if (toCard.getStatus() != Card.Status.ACTIVE) {
            log.error("Карта-получатель ID {} не активна: {}", toCard.getId(), toCard.getStatus());
            throw new InvalidCardOperationException("Карта-получатель не активна");
        }

        if (fromCard.getBalance() < request.getAmount()) {
            log.error("Недостаточно средств на карте ID {}: баланс {}, запрос {}", fromCard.getId(), fromCard.getBalance(), request.getAmount());
            throw new InsufficientFundsException("Недостаточно средств на карте-отправителе");
        }

        fromCard.setBalance(fromCard.getBalance() - request.getAmount());
        toCard.setBalance(toCard.getBalance() + request.getAmount());

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(Transaction.Status.SUCCESS);
        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Перевод успешно выполнен, ID транзакции: {}", savedTransaction.getId());
        return mapToResponse(savedTransaction);
    }

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
    
//    @Override
//    @Transactional(readOnly = true)
//    public List<TransactionResponse> getCardTransactions(String username, Long cardId) {
//
//        Card card = cardRepository.findById(cardId)
//                .orElseThrow(() -> new CardNotFoundException("Card with id " + cardId + " not found"));
//        if (!card.getUser().getUsername().equals(username)) {
//            throw new CardNotFoundException("Card with id " + cardId + " does not belong to user " + username);
//        }
//
//
//        List<Transaction> transactions = transactionRepository.findByFromCardIdOrToCardId(cardId, cardId);
//
//
//        return transactions.stream()
//                .map(transaction -> {
//                    TransactionResponse response = new TransactionResponse();
//                    response.setId(transaction.getId());
//                    response.setFromCardId(transaction.getFromCard().getId());
//                    response.setToCardId(transaction.getToCard().getId());
//                    response.setAmount(transaction.getAmount());
//                    response.setTimestamp(transaction.getTimestamp());
//                    return response;
//                })
//                .collect(Collectors.toList());
//    }
}