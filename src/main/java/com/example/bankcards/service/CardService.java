package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CardService {
    
    CardResponse createCard(CardRequest cardRequest);

    
    CardResponse getCardById(Long id);

    
    Page<CardResponse> getUserCards(Long userId, Pageable pageable);

    
    List<CardResponse> getAllCards();

    
    CardResponse updateCard(Long id, CardUpdateRequest cardUpdateRequest);

    
    CardResponse blockCard(Long id);

    
    CardResponse activateCard(Long id);

    
    void deleteCard(Long id);
}