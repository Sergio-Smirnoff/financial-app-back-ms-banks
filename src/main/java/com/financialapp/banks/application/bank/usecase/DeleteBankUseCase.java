package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.application.bank.command.DeleteBankCommand;

public interface DeleteBankUseCase {
    void execute(DeleteBankCommand command);
}
