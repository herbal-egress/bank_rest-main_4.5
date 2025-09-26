package com.example.bankcards.dto.card;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.bankcards.entity.Card;
import lombok.Data;
import java.time.YearMonth;
@Data
public class CardResponse {
    private Long id;
    private String maskedCardNumber; 
    private String ownerName;
    @JsonFormat(pattern = "yyyy-MM")
    private YearMonth expirationDate;
    private Card.Status status;
    private Double balance;
    private Long userId;
    public static String maskCardNumber(String encryptedCardNumber) {
        if (encryptedCardNumber == null || encryptedCardNumber.length() < 4) {
            return "**** **** **** ****";
        }
        String lastFour = encryptedCardNumber.substring(encryptedCardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}