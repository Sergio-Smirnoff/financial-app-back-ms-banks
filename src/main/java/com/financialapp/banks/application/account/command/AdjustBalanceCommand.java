package com.financialapp.banks.application.account.command;

import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.common.model.Money;

public record AdjustBalanceCommand(AccountId accountId, Money delta) {}
