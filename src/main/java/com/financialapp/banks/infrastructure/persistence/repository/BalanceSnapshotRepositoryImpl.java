package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import com.financialapp.banks.infrastructure.persistence.entity.BalanceSnapshotJpaEntity;
import com.financialapp.banks.infrastructure.persistence.jpa.BalanceSnapshotJpaRepository;
import com.financialapp.banks.infrastructure.persistence.mapper.BalanceSnapshotPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BalanceSnapshotRepositoryImpl implements BalanceSnapshotRepository {

    private final BalanceSnapshotJpaRepository jpaRepository;
    private final BalanceSnapshotPersistenceMapper mapper;

    @Override
    @Transactional
    public BalanceSnapshot save(BalanceSnapshot snapshot) {
        BalanceSnapshotJpaEntity entity = jpaRepository.findByUserIdAndSnapshotDate(
                snapshot.userId().value(), snapshot.snapshotDate())
                .map(existing -> {
                    mapper.merge(existing, snapshot);
                    return existing;
                })
                .orElseGet(() -> mapper.toJpa(snapshot));

        BalanceSnapshotJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceSnapshot> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to) {
        return jpaRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                userId.value(), from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
