package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.DomainException;
import java.util.Map;

public class AccountInvestmentRestrictionException extends DomainException {
    public AccountInvestmentRestrictionException(String cbu) {
        super(DomainError.ACCOUNT_INVESTMENT_RESTRICTION,
              "Cannot manually adjust balance of investment account '" + cbu + "'",
              Map.of("cbu", cbu));
    }
}
