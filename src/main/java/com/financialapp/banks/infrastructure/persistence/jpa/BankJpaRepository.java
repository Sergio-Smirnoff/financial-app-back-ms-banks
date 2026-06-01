package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankJpaRepository extends JpaRepository<BankJpaEntity, Long> {
    Optional<BankJpaEntity> findByName(String name);
    Optional<BankJpaEntity> findByBankNumber(String bankNumber);
    boolean existsByBankNumber(String bankNumber);
}
