package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.common.model.Money;

public record CreateAccountCommand(
    UserId userId,
    BankName bankName,
    String name,
    AccountType type,
    Money initialBalance,
    Boolean isActive,
    String cbu,
    String alias
) {}
