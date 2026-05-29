package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.model.bank.BankName;

import java.util.List;

public interface ListAvailableBanksUseCase {
    List<BankName> execute();
}
