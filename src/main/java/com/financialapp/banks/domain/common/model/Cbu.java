package com.financialapp.banks.domain.common.model;

import com.financialapp.banks.domain.exception.cbu.InvalidCbuException;
import com.financialapp.banks.domain.model.account.AccountNumber;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.SucursalCode;

/**
 * Argentine CBU (Clave Bancaria Uniforme): a bank number, a sucursal code and an
 * account number. The same structure also represents a CVU (fintech/wallet).
 *
 * <pre>
 * Block 1 (8):  bankNumber(3) | sucursalCode(4) | checkDigit1(1)
 * Block 2 (14): accountNumber(13)              | checkDigit2(1)
 * </pre>
 *
 * The parts are stored separately; {@link #value()} joins them into the 22-digit
 * string, computing both BCRA modulo-10 check digits.
 */
public record Cbu(BankNumber bankNumber, SucursalCode sucursalCode, AccountNumber accountNumber) {

    

    /** Parses a 22-digit CBU string into its parts, validating format and both check digits. */
    public static Cbu from(String raw) {
        requireTwentyTwoDigits(raw);
        Cbu cbu = new Cbu(
                new BankNumber(raw.substring(0, 3)),
                new SucursalCode(raw.substring(3, 7)),
                new AccountNumber(raw.substring(8, 21)));
        requireMatchingCheckDigits(raw, cbu);
        return cbu;
    }

    private static final int[] FIRST_BLOCK_WEIGHTS = {7, 1, 3, 9, 7, 1, 3};
    private static final int[] SECOND_BLOCK_WEIGHTS = {3, 9, 7, 1, 3, 9, 7, 1, 3, 9, 7, 1, 3};

    public String value() {
        return firstBlock() + secondBlock();
    }

    private String firstBlock() {
        String body = bankNumber.value() + sucursalCode.value();
        return body + checkDigit(body, FIRST_BLOCK_WEIGHTS);
    }

    private String secondBlock() {
        String body = accountNumber.value();
        return body + checkDigit(body, SECOND_BLOCK_WEIGHTS);
    }

    private static void requireTwentyTwoDigits(String raw) {
        if (raw == null || !raw.matches("\\d{22}")) {
            throw new InvalidCbuException(raw, "must be exactly 22 digits");
        }
    }

    private static void requireMatchingCheckDigits(String raw, Cbu parsed) {
        if (!parsed.value().equals(raw)) {
            throw new InvalidCbuException(raw, "input digits did not pass check digit validation");
        }
    }

    private static int checkDigit(String body, int[] weights) {
        return (10 - (weightedSum(body, weights) % 10)) % 10;
    }

    private static int weightedSum(String body, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (body.charAt(i) - '0') * weights[i];
        }
        return sum;
    }

    @Override
    public String toString() {
        return value();
    }
}
