package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;

public interface GetBankUseCase {
    Bank execute(BankNumber bankNumber);
}
