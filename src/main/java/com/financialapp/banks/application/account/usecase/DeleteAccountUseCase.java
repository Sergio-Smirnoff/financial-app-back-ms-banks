package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.application.account.command.DeleteAccountCommand;

public interface DeleteAccountUseCase {
    void execute(DeleteAccountCommand command);
}
