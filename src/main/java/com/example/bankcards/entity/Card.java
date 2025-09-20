// entity/Card.java
package com.example.bankcards.entity;

// Добавленный код: Импорт необходимых аннотаций JPA, валидации и классов для работы с датами
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.YearMonth;

// Добавленный код: Сущность Card представляет банковскую карту пользователя.
@Entity
@Table(name = "cards")
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

    // Добавленный код: Конструктор по умолчанию (требование JPA)
    public Card() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public YearMonth getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(YearMonth expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}