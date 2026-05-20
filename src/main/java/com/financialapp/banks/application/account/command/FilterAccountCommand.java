package com.financialapp.banks.application.account.command;

import java.util.Currency;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankName;

public record FilterAccountCommand(
    UserId userId,
    AccountType type,
    Currency currency,
    BankName bankName
) {}
