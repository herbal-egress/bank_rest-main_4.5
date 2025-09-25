package com.example.bankcards.dto.transaction;


import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TransactionResponse {

    
    private Long id;

    
    private Long fromCardId;

    
    private Long toCardId;

    
    private Double amount;

    
    private LocalDateTime timestamp;

    
    private String status;
}