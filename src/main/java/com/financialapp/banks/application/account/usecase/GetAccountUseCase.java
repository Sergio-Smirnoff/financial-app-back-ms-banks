package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.domain.model.account.Account;

public interface GetAccountUseCase {
    Account execute(String cbu);
}
