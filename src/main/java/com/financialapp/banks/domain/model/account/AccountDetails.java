package com.financialapp.banks.domain.model.account;

public record AccountDetails(
    String name,
    AccountType type,
    Boolean isActive
) {}
