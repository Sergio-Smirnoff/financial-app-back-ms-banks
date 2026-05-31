package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.InvalidSucursalCodeException;

import java.util.regex.Pattern;

/**
 * The 4-digit sucursal (branch office) code — positions 4–7 of a CBU. Identifies
 * the specific branch of a {@link BankNumber} where an account is domiciled.
 */
public record SucursalCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{4}$");

    public SucursalCode {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidSucursalCodeException(value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
