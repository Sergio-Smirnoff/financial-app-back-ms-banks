package com.financialapp.banks.infrastructure.client.adapter;

import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.infrastructure.client.InvestmentsFeignClient;
import com.financialapp.banks.web.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvestmentsClientAdapter implements InvestmentsPort {

    private final InvestmentsFeignClient client;

    @Override
    public int countHoldings(String accountCbu) {
        ApiResponse<Long> response = client.countHoldings(accountCbu);
        Long count = response != null ? response.getData() : null;
        return count != null ? count.intValue() : 0;
    }

    @Override
    public BigDecimal getPortfolioValuation(String accountCbu) {
        ApiResponse<InvestmentsFeignClient.AccountValuation> response = client.getValuation(accountCbu);
        InvestmentsFeignClient.AccountValuation valuation = response != null ? response.getData() : null;
        return valuation != null ? valuation.totalValuation() : BigDecimal.ZERO;
    }
}
