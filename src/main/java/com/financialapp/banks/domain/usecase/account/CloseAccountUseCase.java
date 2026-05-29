package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.CloseAccountCommand;

public interface CloseAccountUseCase {
    void execute(CloseAccountCommand command);
}
