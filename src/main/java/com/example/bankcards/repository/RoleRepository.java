// repository/RoleRepository.java
package com.example.bankcards.repository;

import com.example.bankcards.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Добавленный код: Репозиторий для работы с сущностью Role.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(Role.RoleType name);
}