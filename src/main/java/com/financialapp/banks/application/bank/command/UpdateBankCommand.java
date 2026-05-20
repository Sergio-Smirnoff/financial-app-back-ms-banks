package com.financialapp.banks.application.bank.command;

import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;

public record UpdateBankCommand(BankName name, Logo logo) {}
