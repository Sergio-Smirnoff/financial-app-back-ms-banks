package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.account.AccountId;

public record UpdateAccountCommand(
    AccountId id,
    String name,
    Money balance,
    Boolean isActive
) {}
