package com.financialapp.banks.application.snapshot.impl;

import com.financialapp.banks.domain.exception.account.InvalidDateRangeException;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.repository.BalanceSnapshotRepository;
import com.financialapp.banks.domain.usecase.snapshot.GetBalanceSnapshots;
import com.financialapp.banks.domain.usecase.snapshot.command.GetBalanceSnapshotsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBalanceSnapshotsUseCaseImpl implements GetBalanceSnapshots {

    private final BalanceSnapshotRepository snapshotRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BalanceSnapshot> execute(GetBalanceSnapshotsCommand command) {
        LocalDate from = command.from() != null ? command.from() : LocalDate.now().minusMonths(1);
        LocalDate to = command.to() != null ? command.to() : LocalDate.now();

        if (from.isAfter(to)) {
            throw new InvalidDateRangeException(from.toString(), to.toString());
        }

        return snapshotRepository.findByUserIdAndDateBetween(command.userId(), from, to);
    }
}
