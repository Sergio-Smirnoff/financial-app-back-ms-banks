package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.CardInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardInstallmentRepository extends JpaRepository<CardInstallment, Long> {
    List<CardInstallment> findByCardIdOrderByDueDateAsc(Long cardId);
    int countByCardAccountIdAndPaidFalse(Long accountId);
}
