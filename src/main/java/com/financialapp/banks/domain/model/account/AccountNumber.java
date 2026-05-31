package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.exception.account.InvalidAccountNumberException;

import java.util.regex.Pattern;

/**
 * The 13-digit account number within a bank — positions 9–21 of a CBU.
 */
public record AccountNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{13}$");

    public AccountNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidAccountNumberException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
