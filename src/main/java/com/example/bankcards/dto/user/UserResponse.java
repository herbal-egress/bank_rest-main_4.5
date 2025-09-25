
package com.example.bankcards.dto.user;

import lombok.Data;

import java.util.Set;


@Data
public class UserResponse {
    private Long id;
    private String username;
    private Set<String> roles;
}