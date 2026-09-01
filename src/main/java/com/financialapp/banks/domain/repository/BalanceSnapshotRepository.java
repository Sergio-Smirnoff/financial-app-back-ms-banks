package com.financialapp.banks.domain.repository;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface BalanceSnapshotRepository {
    BalanceSnapshot save(BalanceSnapshot snapshot);
    List<BalanceSnapshot> findByUserIdAndDateBetween(UserId userId, LocalDate from, LocalDate to);
}
