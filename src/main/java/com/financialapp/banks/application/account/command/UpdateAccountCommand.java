package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.common.model.Money;

public record UpdateAccountCommand(
    String cbu,
    String name,
    Money balance,
    Boolean isActive
) {}
