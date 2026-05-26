package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidCardNumberException;

import java.util.regex.Pattern;

public record CardNumber(String value) {

    private static final Pattern PATTERN = Pattern.compile("^\\d{16}$");

    public CardNumber {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new InvalidCardNumberException(value);
        }
    }

    public static CardNumber of(String value) {
        return new CardNumber(value);
    }

    public String last4() {
        return value.substring(value.length() - 4);
    }

    @Override
    public String toString() {
        return value;
    }
}
