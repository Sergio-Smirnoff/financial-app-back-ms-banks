package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.model.bank.BankName;

public record DeleteAccountCommand(String cbu, BankName bankName) {}
