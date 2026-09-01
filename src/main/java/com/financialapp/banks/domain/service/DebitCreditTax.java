package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.model.account.AccountType;

import java.math.BigDecimal;

public class DebitCreditTax {

    public BigDecimal rate(AccountType accountType) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }
        return switch (accountType) {
            case CHECKING -> new BigDecimal("0.006");
            case SAVINGS -> BigDecimal.ZERO;
        };
    }
}
