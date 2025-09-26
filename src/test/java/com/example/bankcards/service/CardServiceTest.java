package com.example.bankcards.service;
import com.example.bankcards.dto.card.CardRequest;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberAlreadyExistsException;
import com.example.bankcards.exception.InvalidCardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class CardServiceTest {
    @Autowired
    private CardService cardService;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private EncryptionService encryptionService;
    private CardRequest cardRequest;
    private CardUpdateRequest cardUpdateRequest;
    private Card mockCard;
    private User mockUser;
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        cardRequest = new CardRequest();
        cardRequest.setUserId(1L);
        cardRequest.setOwnerName("Test User");
        cardRequest.setExpirationDate(YearMonth.of(2026, 12));
        cardRequest.setBalance(1000.0);
        cardUpdateRequest = new CardUpdateRequest();
        cardUpdateRequest.setOwnerName("Updated User");
        cardUpdateRequest.setExpirationDate(YearMonth.of(2027, 12));
        mockCard = new Card();
        mockCard.setId(1L);
        mockCard.setUser(mockUser);
        mockCard.setOwnerName("Ivan Ivanov");
        mockCard.setEncryptedCardNumber("encrypted_1231111111111111");
        mockCard.setBalance(1000.0);
        mockCard.setStatus(Card.Status.ACTIVE);
        mockCard.setExpirationDate(YearMonth.of(2026, 12));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_9991111111111111");
    }
    @Test
    @WithMockUser(username = "user")
    void createCard_Success() {
        when(cardRepository.existsByEncryptedCardNumber("encrypted_9991111111111111")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        CardResponse response = cardService.createCard(cardRequest);
        assertNotNull(response.getId());
        assertEquals("Ivan Ivanov", response.getOwnerName());
        assertEquals(Card.Status.ACTIVE, response.getStatus());
        assertEquals(1000.0, response.getBalance());
        assertEquals(1L, response.getUserId());
        verify(encryptionService, times(1)).encrypt(anyString());
        verify(cardRepository, times(1)).save(any(Card.class));
    }
    @Test
    @WithMockUser(username = "user")
    void createCard_NegativeBalance_ThrowsException() {
        cardRequest.setBalance(-100.0);
        assertThrows(InvalidCardOperationException.class, () -> cardService.createCard(cardRequest));
        verify(userRepository, never()).findById(anyLong());
    }
    @Test
    @WithMockUser(username = "user")
    void createCard_UserNotFound_ThrowsException() {
        cardRequest.setUserId(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> cardService.createCard(cardRequest));
        verify(userRepository, times(1)).findById(999L);
    }
    @Test
    @WithMockUser(username = "user")
    void createCard_CardNumberExists_ThrowsException() {
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_1231111111111111");
        when(cardRepository.existsByEncryptedCardNumber("encrypted_1231111111111111")).thenReturn(true);
        assertThrows(CardNumberAlreadyExistsException.class, () -> cardService.createCard(cardRequest));
        verify(encryptionService, times(1)).encrypt(anyString());
        verify(cardRepository, times(1)).existsByEncryptedCardNumber("encrypted_1231111111111111");
    }
    @Test
    @WithMockUser(username = "user")
    void getCardById_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        CardResponse response = cardService.getCardById(1L);
        assertEquals(1L, response.getId());
        assertEquals("Ivan Ivanov", response.getOwnerName());
        assertEquals(1000.0, response.getBalance());
        verify(cardRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("user");
    }
    @Test
    @WithMockUser(username = "user")
    void getCardById_NotFound_ThrowsException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(999L));
        verify(cardRepository, times(1)).findById(999L);
    }
    @Test
    @WithMockUser(username = "user")
    void getUserCards_Success() {
        when(cardRepository.findByUserId(1L, PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(mockCard)));
        Page<CardResponse> response = cardService.getUserCards(1L, PageRequest.of(0, 10));
        assertEquals(1, response.getTotalElements());
        verify(cardRepository, times(1)).findByUserId(1L, PageRequest.of(0, 10));
    }
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllCards_Success() {
        when(cardRepository.findAll()).thenReturn(List.of(mockCard));
        List<CardResponse> response = cardService.getAllCards();
        assertEquals(1, response.size());
        verify(cardRepository, times(1)).findAll();
    }
    @Test
    @WithMockUser(username = "user")
    void updateCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(mockUser));
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        CardResponse response = cardService.updateCard(1L, cardUpdateRequest);
        assertEquals("Updated User", response.getOwnerName());
        assertEquals(YearMonth.of(2027, 12), response.getExpirationDate());
        verify(cardRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByUsername("user");
        verify(cardRepository, times(1)).save(any(Card.class));
    }
    @Test
    @WithMockUser(username = "user")
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        CardResponse response = cardService.blockCard(1L);
        assertEquals(Card.Status.BLOCKED, response.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }
    @Test
    @WithMockUser(username = "user")
    void activateCard_Success() {
        mockCard.setStatus(Card.Status.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(cardRepository.save(any(Card.class))).thenReturn(mockCard);
        CardResponse response = cardService.activateCard(1L);
        assertEquals(Card.Status.ACTIVE, response.getStatus());
        verify(cardRepository, times(1)).findById(1L);
        verify(cardRepository, times(1)).save(any(Card.class));
    }
    @Test
    @WithMockUser(username = "user")
    void deleteCard_Success() {
        when(cardRepository.existsById(1L)).thenReturn(true);
        cardService.deleteCard(1L);
        verify(cardRepository, times(1)).existsById(1L);
        verify(cardRepository, times(1)).deleteById(1L);
    }
}