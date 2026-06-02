package com.financialapp.banks.application.account;

import com.financialapp.banks.application.account.impl.GetAccountTransactionsUseCaseImpl;
import com.financialapp.banks.domain.exception.FinancesServiceException;
import com.financialapp.banks.domain.exception.InfrastructureException;
import com.financialapp.banks.domain.port.FinancesPort;
import com.financialapp.banks.domain.port.FinancesPort.TransactionSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountTransactionsUseCaseImplTest {

    @Mock FinancesPort financesPort;
    GetAccountTransactionsUseCaseImpl useCase;

    private static final TransactionSummary TX =
            new TransactionSummary(1L, "0070001600000000123459", null, "Coffee", "Food", "Cafe", LocalDate.of(2026, 5, 1));

    @BeforeEach
    void setUp() {
        useCase = new GetAccountTransactionsUseCaseImpl(financesPort);
    }

    @Test
    void getRecent_returnsPortResult() {
        when(financesPort.getRecentTransactions("cbu", 5)).thenReturn(List.of(TX));
        assertThat(useCase.getRecent("cbu", 5)).containsExactly(TX);
    }

    @Test
    void getRecent_wrapsInfrastructureFailure() {
        when(financesPort.getRecentTransactions(any(), anyInt())).thenThrow(new InfrastructureException("down"));
        assertThatThrownBy(() -> useCase.getRecent("cbu", 5))
                .isInstanceOf(FinancesServiceException.class);
    }

    @Test
    void getAll_returnsPortResult() {
        when(financesPort.getAllTransactions("cbu")).thenReturn(List.of(TX));
        assertThat(useCase.getAll("cbu")).containsExactly(TX);
    }

    @Test
    void getAll_wrapsInfrastructureFailure() {
        when(financesPort.getAllTransactions(any())).thenThrow(new InfrastructureException("down"));
        assertThatThrownBy(() -> useCase.getAll("cbu")).isInstanceOf(FinancesServiceException.class);
    }

    @Test
    void getFiltered_returnsPortResult() {
        var from = LocalDate.of(2026, 5, 1);
        var to = LocalDate.of(2026, 5, 31);
        when(financesPort.getFilteredTransactions("cbu", from, to)).thenReturn(List.of(TX));
        assertThat(useCase.getFiltered("cbu", from, to)).containsExactly(TX);
    }

    @Test
    void getFiltered_wrapsInfrastructureFailure() {
        when(financesPort.getFilteredTransactions(any(), any(), any())).thenThrow(new InfrastructureException("down"));
        assertThatThrownBy(() -> useCase.getFiltered("cbu", LocalDate.now(), LocalDate.now()))
                .isInstanceOf(FinancesServiceException.class);
    }
}
