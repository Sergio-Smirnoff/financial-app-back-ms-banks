package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.CardInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CardInstallmentRepository extends JpaRepository<CardInstallment, Long> {
    List<CardInstallment> findByCardIdOrderByDueDateAsc(Long cardId);
    boolean existsByCardIdAndPaidFalse(Long cardId);
    boolean existsByCardIdAndDescriptionAndAmountAndDueDate(Long cardId, String description, BigDecimal amount, LocalDate dueDate);
}
