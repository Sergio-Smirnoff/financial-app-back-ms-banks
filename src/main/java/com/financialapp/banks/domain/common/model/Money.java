package com.financialapp.banks.domain.common.model;

import com.financialapp.banks.domain.exception.InvalidCurrencyException;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, parseCurrency(currencyCode));
    }

    public static Currency parseCurrency(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidCurrencyException(currencyCode);
        }
    }
}
