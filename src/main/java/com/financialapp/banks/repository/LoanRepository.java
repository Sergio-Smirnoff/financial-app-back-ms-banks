package com.financialapp.banks.repository;

import com.financialapp.banks.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByAccountId(Long accountId);
    Optional<Loan> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT l FROM Loan l WHERE l.active = true AND " +
           "(SELECT MIN(li.dueDate) FROM LoanInstallment li WHERE li.loan = l AND li.paid = false) BETWEEN :from AND :to")
    List<Loan> findActiveWithUpcomingPayment(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
