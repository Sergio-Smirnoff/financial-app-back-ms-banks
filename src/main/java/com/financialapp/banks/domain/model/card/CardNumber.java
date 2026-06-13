package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidCardCheckDigitException;
import com.financialapp.banks.domain.exception.card.InvalidCardNumberException;

/**
 * A 16-digit card PAN (ISO/IEC 7812): an {@link IssuerBin} (6) + an
 * {@link IssuerCardAccount} (9) + a Luhn check digit. The parts are stored
 * separately; {@link #value()} joins them and computes the check digit.
 */
public record CardNumber(IssuerBin issuerBin, IssuerCardAccount issuerCardAccount) {

    /**
     * Parses a card number into its parts. A 15-digit BIN+account has its Luhn check digit
     * computed and appended; a full 16-digit PAN must carry a matching check digit. Wrong
     * length raises {@link InvalidCardNumberException}; a wrong 16th digit raises
     * {@link InvalidCardCheckDigitException}.
     */
    public static CardNumber from(String pan) {
        if (pan != null && pan.matches("\\d{15}")) {
            return new CardNumber(
                    new IssuerBin(pan.substring(0, 6)),
                    new IssuerCardAccount(pan.substring(6, 15)));
        }
        requireSixteenDigits(pan);
        CardNumber cardNumber = new CardNumber(
                new IssuerBin(pan.substring(0, 6)),
                new IssuerCardAccount(pan.substring(6, 15)));
        requireMatchingCheckDigit(pan, cardNumber);
        return cardNumber;
    }

    /** The full 16-digit PAN: parts joined with the computed Luhn check digit. */
    public String value() {
        String payload = issuerBin.value() + issuerCardAccount.value();
        return payload + luhnCheckDigit(payload);
    }

    /** The last four digits of the PAN. */
    public String last4() {
        return value().substring(12);
    }

    /**
     * Masks all but the last four digits so the PAN never leaks into logs or error messages.
     * Use {@link #value()} when the full number is genuinely required.
     */
    @Override
    public String toString() {
        return "*".repeat(12) + last4();
    }

    private static void requireSixteenDigits(String pan) {
        if (pan == null || !pan.matches("\\d{16}")) {
            throw new InvalidCardNumberException(pan);
        }
    }

    private static void requireMatchingCheckDigit(String pan, CardNumber parsed) {
        if (!parsed.value().equals(pan)) {
            throw new InvalidCardCheckDigitException(pan);
        }
    }

    private static int luhnCheckDigit(String payload) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = payload.length() - 1; i >= 0; i--) {
            int digit = payload.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
