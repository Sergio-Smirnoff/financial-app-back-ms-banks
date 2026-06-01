package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.model.bank.Bank;

import java.util.List;

public interface ListAvailableBanksUseCase {
    List<Bank> execute();
}
