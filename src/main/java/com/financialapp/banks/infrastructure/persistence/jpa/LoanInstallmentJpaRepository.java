package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);

    @Query("SELECT li FROM LoanInstallment li WHERE li.paid = false AND li.dueDate BETWEEN :from AND :to")
    List<LoanInstallment> findUpcomingUnpaid(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
