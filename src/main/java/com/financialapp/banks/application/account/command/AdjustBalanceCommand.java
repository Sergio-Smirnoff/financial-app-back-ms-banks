package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.common.model.Money;

public record AdjustBalanceCommand(String accountCbu, Money delta) {}
