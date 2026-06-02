package com.financialapp.banks.application.upcoming;

import com.financialapp.banks.application.upcoming.impl.GetUpcomingPaymentsUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.query.UpcomingInstallment;
import com.financialapp.banks.domain.query.UpcomingInstallmentsQuery;
import com.financialapp.banks.domain.usecase.upcoming.UpcomingPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUpcomingPaymentsUseCaseImplTest {

    @Mock UpcomingInstallmentsQuery query;
    GetUpcomingPaymentsUseCaseImpl useCase;

    private static final Currency ARS = Currency.getInstance("ARS");

    @BeforeEach
    void setUp() {
        useCase = new GetUpcomingPaymentsUseCaseImpl(query);
    }

    private UpcomingInstallment row(long id, LocalDate due) {
        return new UpcomingInstallment(id, "LOAN", "Loan " + id,
                new Money(new BigDecimal("100.00"), ARS), due, 1, 12, false);
    }

    @Test
    void mapsRowsToPaymentsSortedByDueDate() {
        var from = LocalDate.of(2026, 6, 1);
        var to = LocalDate.of(2026, 12, 1);
        var later = row(1L, LocalDate.of(2026, 8, 1));
        var earlier = row(2L, LocalDate.of(2026, 7, 1));
        when(query.findUnpaidBetween(new UserId(1L), from, to)).thenReturn(List.of(later, earlier));

        List<UpcomingPayment> result = useCase.execute(new UserId(1L), from, to);

        assertThat(result).extracting(UpcomingPayment::id).containsExactly(2L, 1L);
        assertThat(result.get(0).description()).isEqualTo("Loan 2");
    }

    @Test
    void returnsEmptyWhenNoInstallments() {
        var from = LocalDate.of(2026, 6, 1);
        var to = LocalDate.of(2026, 12, 1);
        when(query.findUnpaidBetween(new UserId(1L), from, to)).thenReturn(List.of());

        assertThat(useCase.execute(new UserId(1L), from, to)).isEmpty();
    }
}
