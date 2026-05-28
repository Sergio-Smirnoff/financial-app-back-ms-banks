package com.financialapp.banks.domain.usecase.account.command;

import com.financialapp.banks.domain.common.model.Money;

public record UpdateAccountCommand(
    String cbu,
    String name,
    Money balance,
    Boolean isActive
) {}
