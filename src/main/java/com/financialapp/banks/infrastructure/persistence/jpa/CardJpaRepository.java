package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.Card;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    List<Card> findByBankId(Long bankId);
    Optional<Card> findByIdAndUserId(Long id, Long userId);
    boolean existsByBankIdAndBrandAndCardTypeAndLast4Digits(Long bankId, CardBrand brand, CardType cardType, String last4Digits);

    int countByBankId(Long bankId);

    @Query("SELECT c FROM Card c WHERE c.expiringDate BETWEEN :from AND :to")
    List<Card> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
