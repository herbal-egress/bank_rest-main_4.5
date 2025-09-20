// entity/User.java
package com.example.bankcards.entity;

// Добавленный код: Импорт необходимых аннотаций JPA и классов для работы с коллекциями и валидацией
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Добавленный код: Импорт для коллекций
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// Добавленный код: Сущность User представляет пользователя системы. Реализует UserDetails для интеграции с Spring Security.
@Entity
@Table(name = "users")
public class User implements UserDetails {
    // Добавленный код: Первичный ключ, генерируемый БД
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Добавленный код: Имя пользователя (логин). Должно быть уникальным и не пустым.
    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String username;

    // Добавленный код: Пароль. Хранится в зашифрованном виде. Не может быть пустым.
    @NotBlank
    @Column(nullable = false)
    private String password;

    // Добавленный код: Связь ManyToMany с сущностью Role. Пользователь может иметь несколько ролей.
    // FetchType.EAGER - роли загружаются сразу вместе с пользователем (требование Spring Security).
    // CascadeType.PERSIST - роли сохраняются/обновляются вместе с пользователем.
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    // Добавленный код: Определение таблицы связей между users и roles
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Добавленный код: Конструктор по умолчанию (требование JPA)
    public User() {
    }

    // Добавленный код: Конструктор для удобного создания пользователя
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Добавленный код: Методы интерфейса UserDetails. В данном случае, аккаунт всегда активен, не просрочен и не заблокирован.
    // Эти методы можно усложнить, добавив соответствующие поля в сущность, если потребуется.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}