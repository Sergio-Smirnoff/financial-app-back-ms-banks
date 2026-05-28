package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.model.account.Account;

public interface GetAccountUseCase {
    Account execute(String cbu);
}
