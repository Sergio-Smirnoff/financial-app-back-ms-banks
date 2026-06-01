package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidIssuerBinException;

import java.util.regex.Pattern;

/**
 * The 6-digit Issuer BIN (Bank Identification Number, a.k.a. IIN) — the leading
 * digits of a card PAN that identify the network and issuing institution.
 */
public record IssuerBin(String value) {

    private static final Pattern SIX_DIGITS = Pattern.compile("^\\d{6}$");

    public IssuerBin {
        requireSixDigits(value);
    }

    private static void requireSixDigits(String value) {
        if (value == null || !SIX_DIGITS.matcher(value).matches()) {
            throw new InvalidIssuerBinException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
