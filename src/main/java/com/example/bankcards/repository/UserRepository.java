// repository/UserRepository.java
package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Добавленный код: Репозиторий для работы с сущностью User.
 * Предоставляет CRUD операции и поиск по username.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Добавленный код: Поиск пользователя по уникальному имени (username).
    Optional<User> findByUsername(String username);
}