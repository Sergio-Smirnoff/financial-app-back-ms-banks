package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanJpaRepository extends JpaRepository<LoanJpaEntity, Long> {
    List<LoanJpaEntity> findByUserId(Long userId);
    List<LoanJpaEntity> findByBankIdAndUserId(Long bankId, Long userId);
    Optional<LoanJpaEntity> findByIdAndUserId(Long id, Long userId);

    int countByBankId(Long bankId);

    @Query("SELECT l FROM LoanJpaEntity l WHERE l.active = true AND " +
           "(SELECT MIN(li.dueDate) FROM LoanInstallmentJpaEntity li WHERE li.loan = l AND li.paid = false) BETWEEN :from AND :to")
    List<LoanJpaEntity> findActiveWithUpcomingPayment(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
