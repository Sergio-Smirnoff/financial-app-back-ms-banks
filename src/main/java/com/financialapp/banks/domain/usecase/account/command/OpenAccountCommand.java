package com.financialapp.banks.domain.usecase.account.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.common.model.Money;

public record OpenAccountCommand(
    UserId userId,
    BankNumber bankNumber,
    String name,
    AccountType type,
    Money initialBalance,
    Boolean isActive,
    String cbu,
    String alias
) {}
