package com.financialapp.banks.application.bank.command;

import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;

public record CreateBankCommand(BankName name, Logo logo) {}
