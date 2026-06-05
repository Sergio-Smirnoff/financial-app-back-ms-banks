package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.error.DomainException;
import java.util.Map;

public class AccountInsufficientFundsException extends DomainException {
    public AccountInsufficientFundsException(String cbu, Money available, Money requested) {
        super(DomainError.ACCOUNT_INSUFFICIENT_FUNDS,
              "Insufficient funds in account '" + cbu + "'",
              Map.of("cbu", cbu,
                     "availableBalance", available.amount(),
                     "requestedAmount", requested.amount(),
                     "currency", available.currency().getCurrencyCode()));
    }
}
