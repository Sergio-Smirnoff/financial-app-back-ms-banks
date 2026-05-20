package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;

public interface GetBankUseCase {
    Bank execute(BankName name);
}
