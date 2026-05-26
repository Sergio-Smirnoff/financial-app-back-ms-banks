package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;

import java.util.List;

public record BankWithAccounts(
    Bank bank,
    List<Account> accounts
) {}
