package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.application.account.command.AdjustBalanceCommand;

public interface AdjustBalanceUseCase {
    void execute(AdjustBalanceCommand command);
}
