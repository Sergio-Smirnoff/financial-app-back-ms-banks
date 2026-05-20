package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountId;

public interface GetAccountUseCase {
    Account execute(AccountId id);
}
