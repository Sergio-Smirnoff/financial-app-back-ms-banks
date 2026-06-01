package com.financialapp.banks.domain.usecase.account.command;

import com.financialapp.banks.domain.common.model.Money;

public record AdjustBalanceCommand(String accountCbu, Money delta) {}
