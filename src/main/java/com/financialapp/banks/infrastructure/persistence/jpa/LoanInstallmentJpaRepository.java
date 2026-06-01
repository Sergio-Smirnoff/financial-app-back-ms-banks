package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanInstallmentJpaRepository extends JpaRepository<LoanInstallmentJpaEntity, Long> {
    List<LoanInstallmentJpaEntity> findByLoan_IdOrderByInstallmentNumberAsc(Long loanId);

    @Query("SELECT li FROM LoanInstallmentJpaEntity li WHERE li.paid = false AND li.dueDate BETWEEN :from AND :to")
    List<LoanInstallmentJpaEntity> findUpcomingUnpaid(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT li FROM LoanInstallmentJpaEntity li WHERE li.loan.userId = :userId AND li.paid = false AND li.dueDate BETWEEN :from AND :to")
    List<LoanInstallmentJpaEntity> findUpcomingUnpaidByUser(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
