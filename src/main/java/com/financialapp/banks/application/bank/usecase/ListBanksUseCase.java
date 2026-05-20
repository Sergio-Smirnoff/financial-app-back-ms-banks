package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.domain.model.bank.Bank;

import java.util.List;

public interface ListBanksUseCase {
    List<Bank> execute();
}
