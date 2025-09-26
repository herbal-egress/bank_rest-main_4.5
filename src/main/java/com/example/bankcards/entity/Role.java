package com.example.bankcards.entity;
import org.springframework.security.core.GrantedAuthority;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "roles")
@Data 
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    public enum RoleType {
        USER,
        ADMIN
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;
    @Override
    public String getAuthority() {
        return "ROLE_" + name.name();
    }
}