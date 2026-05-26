package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.domain.common.model.UserId;

import java.util.List;

public interface ListBanksUseCase {
    List<BankWithAccounts> execute(UserId userId);
}
