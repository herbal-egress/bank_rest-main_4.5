package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * добавленный код: Интерфейс для сервиса управления банковскими картами.
 */
public interface CardService {
    /**
     * добавленный код: Создаёт новую карту.
     * @param cardRequest DTO с данными карты
     * @return CardResponse с данными созданной карты
     */
    CardResponse createCard(CardRequest cardRequest);

    /**
     * добавленный код: Получает карту по ID.
     * @param id ID карты
     * @return CardResponse с данными карты
     */
    CardResponse getCardById(Long id);

    /**
     * добавленный код: Получает список карт пользователя с пагинацией.
     * @param userId ID пользователя
     * @param pageable Параметры пагинации
     * @return Page<CardResponse> с данными карт
     */
    Page<CardResponse> getUserCards(Long userId, Pageable pageable);

    /**
     * добавленный код: Получает все карты в системе.
     * @return List<CardResponse> со всеми картами
     */
    List<CardResponse> getAllCards();

    /**
     * добавленный код: Обновляет данные карты.
     * @param id ID карты
     * @param cardUpdateRequest DTO с обновлёнными данными
     * @return CardResponse с обновлёнными данными карты
     */
    CardResponse updateCard(Long id, CardUpdateRequest cardUpdateRequest);

    /**
     * добавленный код: Блокирует карту.
     * @param id ID карты
     * @return CardResponse с данными заблокированной карты
     */
    CardResponse blockCard(Long id);

    /**
     * добавленный код: Активирует карту.
     * @param id ID карты
     * @return CardResponse с данными активированной карты
     */
    CardResponse activateCard(Long id);

    /**
     * добавленный код: Удаляет карту по ID.
     * @param id ID карты
     */
    void deleteCard(Long id);
}