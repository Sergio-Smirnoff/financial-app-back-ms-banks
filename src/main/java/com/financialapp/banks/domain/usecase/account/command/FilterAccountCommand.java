package com.financialapp.banks.domain.usecase.account.command;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;

import java.util.Currency;

public record FilterAccountCommand(
    UserId userId,
    String type,
    Currency currency,
    BankName bankName,
    String name,
    boolean hideEmpty
) {}
