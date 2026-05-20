package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.application.bank.command.CreateBankCommand;
import com.financialapp.banks.domain.model.bank.Bank;

public interface CreateBankUseCase {
    Bank execute(CreateBankCommand command);
}
