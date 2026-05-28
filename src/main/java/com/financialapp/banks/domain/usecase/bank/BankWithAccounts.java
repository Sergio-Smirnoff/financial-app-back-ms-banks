package com.financialapp.banks.domain.usecase.bank;

import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.bank.Bank;

import java.util.List;

public record BankWithAccounts(
    Bank bank,
    List<Account> accounts
) {}
