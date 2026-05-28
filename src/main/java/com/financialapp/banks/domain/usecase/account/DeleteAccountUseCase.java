package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.usecase.account.command.DeleteAccountCommand;

public interface DeleteAccountUseCase {
    void execute(DeleteAccountCommand command);
}
