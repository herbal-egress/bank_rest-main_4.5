package com.example.bankcards.entity;

// Добавленный код: Импорт необходимых аннотаций JPA, валидации и классов для работы с датами
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.YearMonth;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// Добавленный код: Сущность Card представляет банковскую карту пользователя.
@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"user"}) // добавленный код: Исключаем поле user из equals/hashCode для избежания проблем с JPA отношениями.
@ToString(exclude = {"user"}) // добавленный код: Исключаем поле user из toString для предотвращения рекурсии при ленивой загрузке.
public class Card {
    // Добавленный код: Первичный ключ, генерируемый БД
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Добавленный код: Зашифрованный номер карты. Хранится в БД в зашифрованном виде с помощью pgcrypto.
    // Не может быть пустым. Уникален.
    @NotBlank
    @Column(unique = true, nullable = false)
    private String encryptedCardNumber;

    // Добавленный код: Имя владельца карты. Не более 50 символов, только английские буквы (валидация будет в DTO/Service).
    // Не может быть пустым.
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String ownerName;

    // Добавленный код: Срок действия карты. Используется тип YearMonth.
    // Конвертируется в столбец БД типа DATE (будет храниться как первое число месяца).
    // Не может быть null.
    @NotNull
    @Column(nullable = false)
    private YearMonth expirationDate;

    // Добавленный код: Статус карты. Хранится в БД как STRING (например, "ACTIVE", "BLOCKED", "EXPIRED").
    // Не может быть null.
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Добавленный код: Баланс карты.
    @NotNull
    @Column(nullable = false)
    private Double balance = 0.0;

    // Добавленный код: Связь ManyToOne с сущностью User. У одной карты один владелец (User).
    // Many карт принадлежат одному User. Загрузка владельца - ленивая (LAZY) по соображениям производительности.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Добавленный код: Enum для статусов карты.
    public enum Status {
        ACTIVE,    // Активна
        BLOCKED,   // Заблокирована
        EXPIRED    // Истек срок
    }
}