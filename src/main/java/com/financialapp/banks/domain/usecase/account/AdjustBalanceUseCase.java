package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.AdjustBalanceCommand;

public interface AdjustBalanceUseCase {
    void execute(AdjustBalanceCommand command);
}
