package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.account.AccountId;

public record DeleteAccountCommand(AccountId id, BankName bankName) {}
