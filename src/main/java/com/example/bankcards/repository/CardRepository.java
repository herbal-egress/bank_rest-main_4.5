package com.example.bankcards.repository;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);
    List<Card> findByUserId(Long userId);
    void deleteAllCardsByUserId(Long userId);
    Page<Card> findByUserId(Long userId, Pageable pageable);
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserId(@Param("userId") Long userId);
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.encryptedCardNumber = :encryptedNumber AND c.id != :cardId")
    boolean existsByEncryptedCardNumberAndIdNot(@Param("encryptedNumber") String encryptedNumber,
                                                @Param("cardId") Long cardId);
}