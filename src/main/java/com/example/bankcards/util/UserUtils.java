package com.example.bankcards.util;
import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;
@Component 
@RequiredArgsConstructor
@Slf4j
public class UserUtils {
    private final RoleRepository roleRepository;
    public Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            Role defaultRole = roleRepository.findByName(Role.RoleType.USER)
                    .orElseThrow(() -> new IllegalStateException("Роль USER не найдена в базе данных"));
            roles.add(defaultRole);
            log.debug("Назначена роль по умолчанию: USER");
        } else {
            for (String roleName : roleNames) {
                Role.RoleType roleType;
                try {
                    roleType = Role.RoleType.valueOf(roleName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.error("Указана несуществующая роль: {}", roleName);
                    throw new IllegalArgumentException("Роль '" + roleName + "' не существует");
                }
                Role role = roleRepository.findByName(roleType)
                        .orElseThrow(() -> new IllegalStateException("Роль " + roleType + " не найдена в базе данных"));
                roles.add(role);
            }
            log.debug("Назначены роли: {}", roleNames);
        }
        return roles;
    }
}