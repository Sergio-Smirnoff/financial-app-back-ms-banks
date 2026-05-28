package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.CreateAccountCommand;
import com.financialapp.banks.domain.model.account.Account;

public interface CreateAccountUseCase {
    Account execute(CreateAccountCommand command);
}
