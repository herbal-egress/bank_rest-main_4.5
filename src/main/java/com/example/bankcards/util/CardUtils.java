package com.example.bankcards.util;
import com.example.bankcards.dto.card.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.time.YearMonth;
@Component
@RequiredArgsConstructor
public class CardUtils {
    private static final String CARD_NUMBER_PREFIX = "3985";
    private static final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;
    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(CARD_NUMBER_PREFIX);
        for (int i = 0; i < 12; i++) {
            cardNumber.append(random.nextInt(10));
        }
        return cardNumber.toString();
    }
    public static Card.Status determineCardStatus(YearMonth expirationDate) {
        YearMonth current = YearMonth.now();
        if (expirationDate.isBefore(current)) {
            return Card.Status.EXPIRED;
        }
        return Card.Status.ACTIVE;
    }
    public void checkCardOwnership(Card card) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            if (!userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                User currentUser = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
                if (!card.getUser().getId().equals(currentUser.getId())) {
                    throw new AccessDeniedException("Доступ к карте запрещен");
                }
            }
        }
    }
    public static CardResponse mapToCardResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setMaskedCardNumber(CardResponse.maskCardNumber(card.getEncryptedCardNumber()));
        response.setOwnerName(card.getOwnerName());
        response.setExpirationDate(card.getExpirationDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setUserId(card.getUser().getId());
        return response;
    }
}