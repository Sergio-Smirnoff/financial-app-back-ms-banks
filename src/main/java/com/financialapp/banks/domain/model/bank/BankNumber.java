package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.InvalidBankNumberException;

import java.util.regex.Pattern;

/**
 * The 3-digit BCRA entity code that identifies a bank. Doubles as the leading
 * prefix of every CBU issued by that bank.
 */
public record BankNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{3}$");

    public BankNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidBankNumberException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
