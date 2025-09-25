package com.example.bankcards.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    
    @Column(nullable = false)
    private Double amount;

    
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    
    public enum Status {
        SUCCESS,  
        FAILED,   
        PENDING   
    }
}