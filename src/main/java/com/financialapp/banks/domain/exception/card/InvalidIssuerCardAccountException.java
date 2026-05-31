package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class InvalidIssuerCardAccountException extends DomainException {
    public InvalidIssuerCardAccountException(String value) {
        super(DomainError.INVALID_ISSUER_CARD_ACCOUNT,
              "Issuer card account must be exactly 9 digits",
              Map.of("issuerCardAccount", value == null ? "null" : value));
    }
}
