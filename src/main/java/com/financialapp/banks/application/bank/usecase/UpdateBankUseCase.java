package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.application.bank.command.UpdateBankCommand;
import com.financialapp.banks.domain.model.bank.Bank;

public interface UpdateBankUseCase {
    Bank execute(UpdateBankCommand command);
}
