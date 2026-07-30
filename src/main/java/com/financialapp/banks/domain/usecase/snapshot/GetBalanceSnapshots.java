package com.financialapp.banks.domain.usecase.snapshot;

import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import com.financialapp.banks.domain.usecase.snapshot.command.GetBalanceSnapshotsCommand;

import java.util.List;

public interface GetBalanceSnapshots {
    List<BalanceSnapshot> execute(GetBalanceSnapshotsCommand command);
}
