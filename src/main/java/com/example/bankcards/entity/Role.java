package com.example.bankcards.entity;

import org.springframework.security.core.GrantedAuthority;

// Добавленный код: Импорт необходимых аннотаций JPA
import jakarta.persistence.*;

import lombok.Data;

// Добавленный код: Сущность Role для хранения ролей пользователей (USER, ADMIN) в БД. Реализует GrantedAuthority для интеграции с Spring Security.
@Entity
@Table(name = "roles")
@Data // добавленный код: @Data генерирует getters, setters, toString, equals, hashCode для простой сущности без сложных отношений.
public class Role implements GrantedAuthority {
    // Добавленный код: Первичный ключ, генерируемый БД
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Добавленный код: Enum для типов ролей. Хранится в БД как STRING (например, "USER", "ADMIN")
    public enum RoleType {
        USER,
        ADMIN
    }

    // Добавленный код: Поле для хранения типа роли. Не может быть null.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    // Добавленный код: Реализация метода из интерфейса GrantedAuthority. Возвращает имя роли с префиксом "ROLE_", как ожидает Spring Security.
    @Override
    public String getAuthority() {
        return "ROLE_" + name.name();
    }
}