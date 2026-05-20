package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.model.card.CardType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository {
    List<Card> findByUserId(UserId userId);
    List<Card> findByBankName(BankName bankName);
    Optional<Card> findById(CardId id);
    Optional<Card> findByIdAndUserId(CardId id, UserId userId);
    boolean existsByBankNameAndBrandAndTypeAndCardNumber(BankName bankName, CardBrand brand, CardType type, String cardNumber);
    int countByBankName(BankName bankName);
    List<Card> findExpiringBetween(LocalDate from, LocalDate to);
    Card save(Card card);
    void delete(CardId id);
}
