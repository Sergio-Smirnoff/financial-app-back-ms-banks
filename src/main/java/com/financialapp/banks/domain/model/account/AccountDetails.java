package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.common.model.Money;

public record AccountDetails(
    String name,
    AccountType type,
    Money balance,
    Boolean isActive
) {}
