package com.financialapp.banks.domain.usecase.account;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.usecase.account.command.*;

import java.util.List;

public interface ListAccountsUseCase {
    List<Account> execute(FilterAccountCommand command);
}
