package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.Card;
import com.financialapp.banks.model.enums.CardBrand;
import com.financialapp.banks.model.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long userId);
    List<Card> findByAccountId(Long accountId);
    Optional<Card> findByIdAndUserId(Long id, Long userId);
    boolean existsByAccountIdAndBrandAndCardTypeAndLast4Digits(Long accountId, CardBrand brand, CardType cardType, String last4Digits);
}
