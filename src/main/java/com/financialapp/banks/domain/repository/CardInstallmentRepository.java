package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.model.card.CardId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;

import com.financialapp.banks.domain.common.model.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardInstallmentRepository extends InstallmentRepository<CardInstallment, CardInstallmentId, CardId> {
    List<CardInstallment> findByCardId(CardId cardId);
    Optional<CardInstallment> findById(CardInstallmentId id);
    boolean existsByCardIdAndUnpaid(CardId cardId);
    boolean existsByCardIdAndDescriptionAndAmountAndDueDate(CardId cardId, String description, Money amount, LocalDate dueDate);
    CardInstallment save(CardInstallment installment);
    List<CardInstallment> saveAll(List<CardInstallment> installments);
    List<CardInstallment> findUpcomingUnpaid(com.financialapp.banks.domain.common.model.UserId userId, LocalDate from, LocalDate to);
}
