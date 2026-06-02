package com.financialapp.banks.infrastructure.client.adapter;

import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.port.FinancesPort.TransactionSummary;
import com.financialapp.banks.infrastructure.client.FinancesFeignClient;
import com.financialapp.banks.infrastructure.client.FinancesFeignClient.TransactionDto;
import com.financialapp.banks.infrastructure.client.dto.ExternalApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancesClientAdapterTest {

    @Mock FinancesFeignClient client;
    FinancesClientAdapter adapter;

    private static final String CBU = "0070001600000000123459";

    @BeforeEach
    void setUp() {
        adapter = new FinancesClientAdapter(client);
    }

    @Test
    void getRecentTransactions_mapsDtosToSummaries() {
        // Given the finances service returns one transaction
        TransactionDto dto = new TransactionDto(1L, CBU, new BigDecimal("-1500.00"), "ARS",
                "Coffee", "Food", "Cafe", LocalDate.of(2026, 5, 20));
        when(client.getTransactions(eq(CBU), eq(5), isNull(), isNull()))
                .thenReturn(new ExternalApiResponse<>(List.of(dto)));

        // When fetching recent transactions
        List<TransactionSummary> result = adapter.getRecentTransactions(CBU, 5);

        // Then the dto is mapped to a domain summary
        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Coffee");
        assertThat(result.get(0).amount().amount()).isEqualByComparingTo("-1500.00");
    }

    @Test
    void getAllTransactions_returnsEmpty_whenResponseNull() {
        // Given a null envelope from the service
        when(client.getTransactions(eq(CBU), isNull(), isNull(), isNull())).thenReturn(null);

        // When / Then an empty list is returned (no failure)
        assertThat(adapter.getAllTransactions(CBU)).isEmpty();
    }

    @Test
    void getFilteredTransactions_returnsEmpty_whenDataNull() {
        // Given an envelope whose data is null
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);
        when(client.getTransactions(eq(CBU), isNull(), eq(from), eq(to)))
                .thenReturn(new ExternalApiResponse<>(null));

        // When / Then an empty list is returned
        assertThat(adapter.getFilteredTransactions(CBU, from, to)).isEmpty();
    }

    @Test
    void wrapsClientFailureAsInfrastructureException() {
        // Given the feign client fails
        when(client.getTransactions(any(), any(), any(), any())).thenThrow(new RuntimeException("boom"));

        // When / Then the failure is translated to an InfrastructureException
        assertThatThrownBy(() -> adapter.getAllTransactions(CBU))
                .isInstanceOf(InfrastructureException.class)
                .hasMessageContaining("ms-finances");
    }
}
