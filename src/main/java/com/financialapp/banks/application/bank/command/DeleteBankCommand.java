package com.financialapp.banks.application.bank.command;

import com.financialapp.banks.domain.model.bank.BankName;

public record DeleteBankCommand(BankName name) {}
