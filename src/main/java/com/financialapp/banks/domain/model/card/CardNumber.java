package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidCardNumberException;

import java.util.regex.Pattern;

public record CardNumber(String value) {

    private static final Pattern CARD_NUMBER_FORMAT_VALIDATOR = Pattern.compile("^\\d{16}$");

    public CardNumber {
        if (value == null || !CARD_NUMBER_FORMAT_VALIDATOR.matcher(value).matches()) {
            throw new InvalidCardNumberException(value);
        }
    }

    public String last4() {
        return value.substring(value.length() - 4);
    }

    /**
     * Masks all but the last four digits so the PAN never leaks into logs or error messages.
     * Use {@link #value()} when the full number is genuinely required.
     */
    @Override
    public String toString() {
        return "*".repeat(value.length() - 4) + last4();
    }
}
