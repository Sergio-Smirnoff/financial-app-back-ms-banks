package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.Card;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardRepository {
    List<Card> findByUserId(UserId userId);
    List<Card> findByBankNumber(BankNumber bankNumber);
    int countByBankNumber(BankNumber bankNumber);
    Optional<Card> findByCardNumber(String cardNumber);
    Optional<Card> findByCardNumberAndUserId(String cardNumber, UserId userId);
    boolean existsByBankNumberAndBrandAndTypeAndCardNumber(BankNumber bankNumber, CardBrand brand, CardType type, String cardNumber);
    List<Card> findExpiringBetween(LocalDate from, LocalDate to);
    List<CardInstallment> findUpcomingUnpaidInstallments(UserId userId, LocalDate from, LocalDate to);
    Card save(Card card);
    void delete(String cardNumber);
}
