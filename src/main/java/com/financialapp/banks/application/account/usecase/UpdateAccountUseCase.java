package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.application.account.command.UpdateAccountCommand;
import com.financialapp.banks.domain.model.account.Account;

public interface UpdateAccountUseCase {
    Account execute(UpdateAccountCommand command);
}
