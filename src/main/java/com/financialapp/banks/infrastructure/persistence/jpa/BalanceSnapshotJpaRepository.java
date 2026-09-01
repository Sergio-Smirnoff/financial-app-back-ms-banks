package com.financialapp.banks.infrastructure.persistence.jpa;

import com.financialapp.banks.infrastructure.persistence.entity.BalanceSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BalanceSnapshotJpaRepository extends JpaRepository<BalanceSnapshotJpaEntity, Long> {
    Optional<BalanceSnapshotJpaEntity> findByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);
    List<BalanceSnapshotJpaEntity> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(Long userId, LocalDate from, LocalDate to);
}
