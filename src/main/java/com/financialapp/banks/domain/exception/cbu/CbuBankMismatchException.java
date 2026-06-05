package com.financialapp.banks.domain.exception.cbu;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class CbuBankMismatchException extends DomainException {
    public CbuBankMismatchException(String bankNumber, String cbuEntityCode) {
        super(DomainError.CBU_BANK_MISMATCH,
              "CBU entity code " + cbuEntityCode + " does not match bank " + bankNumber,
              Map.of("bankNumber", bankNumber, "cbuEntityCode", cbuEntityCode));
    }
}
