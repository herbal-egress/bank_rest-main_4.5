package com.example.bankcards.repository;

// добавленный код: Импорт JpaRepository и сущности
import com.example.bankcards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// добавленный код: Репозиторий для работы с сущностью Transaction
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromCardIdOrToCardId(Long cardId, Long cardId1);
    // добавленный код: Здесь можно добавить кастомные запросы, если потребуется (например, по картам)
}