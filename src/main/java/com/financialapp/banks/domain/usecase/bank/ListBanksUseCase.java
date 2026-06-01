package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.common.model.UserId;

import java.util.List;

public interface ListBanksUseCase {
    List<BankWithAccounts> execute(UserId userId);
}
