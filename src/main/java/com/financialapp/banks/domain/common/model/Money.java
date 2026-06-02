package com.financialapp.banks.domain.common.model;

import com.financialapp.banks.domain.exception.InvalidCurrencyException;
import com.financialapp.banks.domain.exception.account.AccountCurrencyMismatchException;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, parseCurrency(currencyCode));
    }

    /** Builds money from a decimal string amount, keeping {@link BigDecimal} out of the web layer. */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), parseCurrency(currencyCode));
    }

    /** Zero amount in the given currency. */
    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, parseCurrency(currencyCode));
    }

    public static Currency parseCurrency(String currencyCode) {
        try {
            return Currency.getInstance(currencyCode.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new InvalidCurrencyException(currencyCode);
        }
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isLessThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) < 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new AccountCurrencyMismatchException(
                currency.getCurrencyCode(), other.currency.getCurrencyCode());
        }
    }
}
