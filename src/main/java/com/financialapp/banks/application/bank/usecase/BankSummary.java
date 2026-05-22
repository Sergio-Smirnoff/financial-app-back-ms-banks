package com.financialapp.banks.application.bank.usecase;

import com.financialapp.banks.domain.model.bank.Bank;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

public record BankSummary(
    Bank bank,
    int accountsCount,
    int cardsCount,
    int loansCount,
    Map<Currency, BigDecimal> totalBalances
) {}
