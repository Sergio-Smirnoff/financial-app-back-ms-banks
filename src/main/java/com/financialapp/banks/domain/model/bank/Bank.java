package com.financialapp.banks.domain.model.bank;

public record Bank(
    BankNumber bankNumber,
    String name,
    Logo logo
) {}
