package com.financialapp.banks.application.account.usecase;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.application.account.command.*;

import java.util.List;

public interface ListAccountsUseCase {
    List<Account> execute(FilterAccountCommand command);
}
