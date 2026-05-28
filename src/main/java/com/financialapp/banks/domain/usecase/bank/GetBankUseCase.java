package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;

public interface GetBankUseCase {
    Bank execute(BankName name);
}
