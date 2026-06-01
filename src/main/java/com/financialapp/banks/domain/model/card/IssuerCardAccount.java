package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidIssuerCardAccountException;

import java.util.regex.Pattern;

/**
 * The 9-digit issuer-assigned account identifier within a card PAN — the digits
 * between the {@link IssuerBin} and the Luhn check digit.
 */
public record IssuerCardAccount(String value) {

    private static final Pattern NINE_DIGITS = Pattern.compile("^\\d{9}$");

    public IssuerCardAccount {
        requireNineDigits(value);
    }

    private static void requireNineDigits(String value) {
        if (value == null || !NINE_DIGITS.matcher(value).matches()) {
            throw new InvalidIssuerCardAccountException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
