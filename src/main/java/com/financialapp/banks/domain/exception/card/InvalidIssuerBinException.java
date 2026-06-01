package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class InvalidIssuerBinException extends DomainException {
    public InvalidIssuerBinException(String value) {
        super(DomainError.INVALID_ISSUER_BIN,
              "Issuer BIN must be exactly 6 digits",
              Map.of("issuerBin", value == null ? "null" : value));
    }
}
