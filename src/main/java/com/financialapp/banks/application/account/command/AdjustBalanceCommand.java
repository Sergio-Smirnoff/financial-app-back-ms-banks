package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.model.account.AccountId;

import java.math.BigDecimal;

public record AdjustBalanceCommand(AccountId accountId, BigDecimal delta, String expectedCurrency) {}
