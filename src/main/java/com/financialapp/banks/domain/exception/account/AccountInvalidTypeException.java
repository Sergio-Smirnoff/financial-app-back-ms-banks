package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class AccountInvalidTypeException extends DomainException {
    public AccountInvalidTypeException(String subtype) {
        super(DomainError.ACCOUNT_INVALID_TYPE,
              "Unknown account type: '" + subtype + "'",
              Map.of("subtype", subtype));
    }
}
