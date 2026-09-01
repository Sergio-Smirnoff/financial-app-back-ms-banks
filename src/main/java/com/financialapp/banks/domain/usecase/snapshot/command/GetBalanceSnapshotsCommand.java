package com.financialapp.banks.domain.usecase.snapshot.command;

import com.financialapp.banks.domain.common.model.UserId;

import java.time.LocalDate;

public record GetBalanceSnapshotsCommand(
        UserId userId,
        LocalDate from,
        LocalDate to
) {}
