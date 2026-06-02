package com.financialapp.banks.infrastructure.client.adapter;

import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.infrastructure.client.InvestmentsFeignClient;
import com.financialapp.banks.infrastructure.client.InvestmentsFeignClient.AccountValuation;
import com.financialapp.banks.infrastructure.client.dto.ExternalApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentsClientAdapterTest {

    @Mock InvestmentsFeignClient client;
    InvestmentsClientAdapter adapter;

    private static final String CBU = "0070001600000000123459";

    @BeforeEach
    void setUp() {
        adapter = new InvestmentsClientAdapter(client);
    }

    @Test
    void countHoldings_returnsValue_whenPresent() {
        // Given the service reports 3 holdings
        when(client.countHoldings(CBU)).thenReturn(new ExternalApiResponse<>(3L));

        // When / Then the count is returned as an int
        assertThat(adapter.countHoldings(CBU)).isEqualTo(3);
    }

    @Test
    void countHoldings_returnsZero_whenResponseNull() {
        // Given a null envelope
        when(client.countHoldings(CBU)).thenReturn(null);

        // When / Then it defaults to zero
        assertThat(adapter.countHoldings(CBU)).isZero();
    }

    @Test
    void countHoldings_returnsZero_whenDataNull() {
        // Given an envelope with null data
        when(client.countHoldings(CBU)).thenReturn(new ExternalApiResponse<>(null));

        // When / Then it defaults to zero
        assertThat(adapter.countHoldings(CBU)).isZero();
    }

    @Test
    void countHoldings_wrapsFailure() {
        // Given the client fails
        when(client.countHoldings(any())).thenThrow(new RuntimeException("down"));

        // When / Then it is translated to an InfrastructureException
        assertThatThrownBy(() -> adapter.countHoldings(CBU))
                .isInstanceOf(InfrastructureException.class)
                .hasMessageContaining("ms-investments");
    }

    @Test
    void getPortfolioValuation_returnsTotal_whenPresent() {
        // Given the service reports a valuation
        when(client.getValuation(CBU))
                .thenReturn(new ExternalApiResponse<>(new AccountValuation(CBU, new BigDecimal("1000.00"), "ARS")));

        // When / Then the total valuation is returned
        assertThat(adapter.getPortfolioValuation(CBU)).isEqualByComparingTo("1000.00");
    }

    @Test
    void getPortfolioValuation_returnsZero_whenResponseNull() {
        // Given a null envelope
        when(client.getValuation(CBU)).thenReturn(null);

        // When / Then it defaults to zero
        assertThat(adapter.getPortfolioValuation(CBU)).isEqualByComparingTo("0");
    }

    @Test
    void getPortfolioValuation_returnsZero_whenDataNull() {
        // Given an envelope with null data
        when(client.getValuation(CBU)).thenReturn(new ExternalApiResponse<>(null));

        // When / Then it defaults to zero
        assertThat(adapter.getPortfolioValuation(CBU)).isEqualByComparingTo("0");
    }

    @Test
    void getPortfolioValuation_wrapsFailure() {
        // Given the client fails
        when(client.getValuation(any())).thenThrow(new RuntimeException("down"));

        // When / Then it is translated to an InfrastructureException
        assertThatThrownBy(() -> adapter.getPortfolioValuation(CBU))
                .isInstanceOf(InfrastructureException.class)
                .hasMessageContaining("ms-investments");
    }
}
