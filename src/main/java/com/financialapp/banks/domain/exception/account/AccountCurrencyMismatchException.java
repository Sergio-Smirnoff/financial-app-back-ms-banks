package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class AccountCurrencyMismatchException extends DomainException {
    public AccountCurrencyMismatchException(String accountCurrency, String requestCurrency) {
        super(DomainError.ACCOUNT_CURRENCY_MISMATCH,
              "Account currency '" + accountCurrency + "' does not match requested currency '" + requestCurrency + "'",
              Map.of("accountCurrency", accountCurrency, "requestCurrency", requestCurrency));
    }
}
