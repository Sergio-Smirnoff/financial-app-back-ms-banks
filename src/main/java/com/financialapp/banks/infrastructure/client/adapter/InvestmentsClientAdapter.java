package com.financialapp.banks.infrastructure.client.adapter;

import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.port.InvestmentsPort;
import com.financialapp.banks.infrastructure.client.InvestmentsFeignClient;
import com.financialapp.banks.infrastructure.client.dto.ExternalApiResponse;
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
        try {
            ExternalApiResponse<Long> response = client.countHoldings(accountCbu);
            Long count = response != null ? response.data() : null;
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.error("ms-investments call failed [countHoldings] for accountCbu={}: {}", accountCbu, e.getMessage(), e);
            throw new InfrastructureException("ms-investments: " + e.getMessage());
        }
    }

    @Override
    public BigDecimal getPortfolioValuation(String accountCbu) {
        try {
            ExternalApiResponse<InvestmentsFeignClient.AccountValuation> response = client.getValuation(accountCbu);
            InvestmentsFeignClient.AccountValuation valuation = response != null ? response.data() : null;
            return valuation != null ? valuation.totalValuation() : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("ms-investments call failed [getPortfolioValuation] for accountCbu={}: {}", accountCbu, e.getMessage(), e);
            throw new InfrastructureException("ms-investments: " + e.getMessage());
        }
    }
}
