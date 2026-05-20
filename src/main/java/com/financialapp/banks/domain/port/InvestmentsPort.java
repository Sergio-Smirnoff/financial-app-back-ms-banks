package com.financialapp.banks.domain.port;

import java.math.BigDecimal;

public interface InvestmentsPort {
    int countHoldings(String accountCbu);
    BigDecimal getPortfolioValuation(String accountCbu);
}
