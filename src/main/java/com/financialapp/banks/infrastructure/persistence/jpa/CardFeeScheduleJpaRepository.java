package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.CardFeeScheduleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardFeeScheduleJpaRepository extends JpaRepository<CardFeeScheduleJpaEntity, Long> {
    Optional<CardFeeScheduleJpaEntity> findByCardNumber(String cardNumber);

    @Query("SELECT s FROM CardFeeScheduleJpaEntity s JOIN CardJpaEntity c ON s.cardNumber = c.cardNumber WHERE c.userId = :userId")
    List<CardFeeScheduleJpaEntity> findByUserId(@Param("userId") Long userId);
}
