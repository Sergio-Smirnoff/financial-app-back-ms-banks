package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.application.account.command.CreateAccountCommand;
import com.financialapp.banks.domain.model.account.Account;

public interface CreateAccountUseCase {
    Account execute(CreateAccountCommand command);
}
