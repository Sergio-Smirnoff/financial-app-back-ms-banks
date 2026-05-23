package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardJpaRepository extends JpaRepository<CardJpaEntity, Long> {
    List<CardJpaEntity> findByUserId(Long userId);
    List<CardJpaEntity> findByBankId(Long bankId);
    Optional<CardJpaEntity> findByCardNumber(String cardNumber);
    Optional<CardJpaEntity> findByCardNumberAndUserId(String cardNumber, Long userId);
    boolean existsByBankIdAndBrandAndCardTypeAndCardNumber(Long bankId, CardBrand brand, CardType cardType, String cardNumber);
    int countByBankId(Long bankId);
    void deleteByCardNumber(String cardNumber);

    @Query("SELECT c FROM CardJpaEntity c WHERE c.expiringDate BETWEEN :from AND :to")
    List<CardJpaEntity> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
