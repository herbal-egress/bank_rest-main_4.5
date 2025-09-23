package com.example.bankcards.entity;

// добавленный код: Импорт необходимых аннотаций JPA и Lombok
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// добавленный код: Сущность Transaction представляет транзакцию (перевод) между картами.
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    // добавленный код: Первичный ключ, генерируемый БД
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // добавленный код: Карта-отправитель (ManyToOne, не null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    // добавленный код: Карта-получатель (ManyToOne, не null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    // добавленный код: Сумма перевода (не null, положительная)
    @Column(nullable = false)
    private Double amount;

    // добавленный код: Дата и время транзакции (автоматически устанавливается)
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // добавленный код: Статус транзакции (SUCCESS, FAILED и т.д.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // добавленный код: Enum для статусов транзакции
    public enum Status {
        SUCCESS,  // Успешно завершена
        FAILED,   // Не удалась
        PENDING   // В обработке (для будущего расширения)
    }
}