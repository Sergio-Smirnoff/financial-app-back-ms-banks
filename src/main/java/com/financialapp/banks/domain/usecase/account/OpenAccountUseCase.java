package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.OpenAccountCommand;
import com.financialapp.banks.domain.model.account.Account;

public interface OpenAccountUseCase {
    Account execute(OpenAccountCommand command);
}
