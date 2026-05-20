package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardInstallmentRepository extends InstallmentRepository<CardInstallment, CardInstallmentId, String> {
    List<CardInstallment> findByCardNumber(String cardNumber);
    Optional<CardInstallment> findById(CardInstallmentId id);
    boolean existsByCardNumberAndUnpaid(String cardNumber);
    boolean existsByCardNumberAndDescriptionAndAmountAndDueDate(String cardNumber, String description, Money amount, LocalDate dueDate);
    CardInstallment save(CardInstallment installment);
    List<CardInstallment> saveAll(List<CardInstallment> installments);
    List<CardInstallment> findUpcomingUnpaid(UserId userId, LocalDate from, LocalDate to);
}
