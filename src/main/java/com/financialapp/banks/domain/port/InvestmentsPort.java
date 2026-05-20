package com.financialapp.banks.domain.port;

import com.financialapp.banks.domain.model.account.AccountId;

import java.math.BigDecimal;

public interface InvestmentsPort {
    int countHoldings(AccountId accountId);
    BigDecimal getPortfolioValuation(AccountId accountId);
}
