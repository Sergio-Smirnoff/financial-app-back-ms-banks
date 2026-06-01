package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.UpdateAccountCommand;
import com.financialapp.banks.domain.model.account.Account;

public interface UpdateAccountUseCase {
    Account execute(UpdateAccountCommand command);
}
