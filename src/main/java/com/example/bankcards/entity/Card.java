package com.example.bankcards.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.YearMonth;


import com.example.bankcards.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"user"})
@ToString(exclude = {"user"})
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String encryptedCardNumber;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String ownerName;

    @NotNull
    @Column(nullable = false)
    private YearMonth expirationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotNull
    @Column(nullable = false)
    private Double balance = 0.0;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    
    @OneToMany(mappedBy = "fromCard", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactionsFrom = new ArrayList<>();

    
    @OneToMany(mappedBy = "toCard", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactionsTo = new ArrayList<>();

    public enum Status {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }
}