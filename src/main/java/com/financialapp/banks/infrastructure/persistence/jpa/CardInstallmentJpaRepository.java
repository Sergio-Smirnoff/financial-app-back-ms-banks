package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CardInstallmentJpaRepository extends JpaRepository<CardInstallmentJpaEntity, Long> {
    List<CardInstallmentJpaEntity> findByCard_CardNumberOrderByDueDateAsc(String cardNumber);
    boolean existsByCard_CardNumberAndPaidFalse(String cardNumber);
    boolean existsByCard_CardNumberAndDescriptionAndAmountAndDueDate(String cardNumber, String description, BigDecimal amount, LocalDate dueDate);

    @Query("SELECT ci FROM CardInstallmentJpaEntity ci WHERE ci.card.userId = :userId AND ci.paid = false AND ci.dueDate BETWEEN :from AND :to")
    List<CardInstallmentJpaEntity> findUpcomingUnpaid(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT ci FROM CardInstallmentJpaEntity ci WHERE ci.paid = false AND ci.dueDate BETWEEN :from AND :to")
    List<CardInstallmentJpaEntity> findAllUpcomingUnpaid(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
