package com.financialapp.banks.domain.gateway;

import java.math.BigDecimal;

public interface InvestmentsGateway {
    int countHoldings(String accountCbu);
    BigDecimal getPortfolioValuation(String accountCbu);
}
