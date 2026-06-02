package com.financialapp.banks.application.upcoming;

import com.financialapp.banks.application.upcoming.impl.GetUpcomingPaymentsUseCaseImpl;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.repository.CardRepository;
import com.financialapp.banks.domain.repository.LoanRepository;
import com.financialapp.banks.domain.usecase.upcoming.UpcomingPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUpcomingPaymentsUseCaseImplTest {

    @Mock LoanRepository loanRepository;
    @Mock CardRepository cardRepository;
    GetUpcomingPaymentsUseCaseImpl useCase;

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final UserId USER = new UserId(1L);
    private static final LocalDate FROM = LocalDate.of(2026, 6, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 1);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    @BeforeEach
    void setUp() {
        useCase = new GetUpcomingPaymentsUseCaseImpl(loanRepository, cardRepository);
    }

    private LoanInstallment loanInstallment(long id, int number, LocalDate due, boolean paid) {
        return new LoanInstallment(new LoanInstallmentId(id), new LoanId(10L), number,
                new Money(new BigDecimal("100.00"), ARS), due, paid, paid ? due : null, NOW, NOW);
    }

    private Loan loanWith(List<LoanInstallment> installments) {
        return new Loan(new LoanId(10L), USER, new BankNumber("007"), "Car Loan",
                new Money(new BigDecimal("1200.00"), ARS), new BigDecimal("12.00"),
                12, 12, AmortizationType.FRENCH, FROM, true, installments, NOW, NOW);
    }

    private CardInstallment cardInstallment(long id, int number, LocalDate due) {
        return new CardInstallment(new CardInstallmentId(id), "4509790000000009", "Laptop",
                new Money(new BigDecimal("3000.00"), ARS), number, 6,
                new Money(new BigDecimal("500.00"), ARS), due, false, null, NOW, NOW);
    }

    @Test
    void mergesAndSortsLoanAndCardPaymentsByDueDate() {
        // Given one upcoming loan installment (Aug) and one card installment (Jul)
        Loan loan = loanWith(List.of(loanInstallment(1L, 3, LocalDate.of(2026, 8, 1), false)));
        when(loanRepository.findWithUpcomingUnpaidInstallments(USER, FROM, TO)).thenReturn(List.of(loan));
        when(cardRepository.findUpcomingUnpaidInstallments(USER, FROM, TO))
                .thenReturn(List.of(cardInstallment(2L, 1, LocalDate.of(2026, 7, 1))));

        // When listing upcoming payments
        List<UpcomingPayment> result = useCase.execute(USER, FROM, TO);

        // Then both are returned, sorted by due date (card first), each with its source type
        assertThat(result).extracting(UpcomingPayment::id).containsExactly(2L, 1L);
        assertThat(result).extracting(UpcomingPayment::type).containsExactly("CARD", "LOAN");
        assertThat(result.get(0).description()).isEqualTo("Laptop");
        assertThat(result.get(0).totalInstallments()).isEqualTo(6);
        assertThat(result.get(1).description()).isEqualTo("Car Loan");
        assertThat(result.get(1).totalInstallments()).isEqualTo(12);
    }

    @Test
    void filtersOutPaidAndOutOfRangeLoanInstallments() {
        // Given a loan whose installments include a paid one, an out-of-range one, and one valid
        Loan loan = loanWith(List.of(
                loanInstallment(1L, 1, LocalDate.of(2026, 7, 1), true),    // paid -> excluded
                loanInstallment(2L, 2, LocalDate.of(2026, 5, 1), false),   // before range -> excluded
                loanInstallment(3L, 3, LocalDate.of(2027, 1, 1), false),   // after range -> excluded
                loanInstallment(4L, 4, LocalDate.of(2026, 9, 1), false))); // valid -> included
        when(loanRepository.findWithUpcomingUnpaidInstallments(USER, FROM, TO)).thenReturn(List.of(loan));
        when(cardRepository.findUpcomingUnpaidInstallments(USER, FROM, TO)).thenReturn(List.of());

        // When listing upcoming payments
        List<UpcomingPayment> result = useCase.execute(USER, FROM, TO);

        // Then only the unpaid in-range installment survives
        assertThat(result).extracting(UpcomingPayment::id).containsExactly(4L);
    }

    @Test
    void returnsEmptyWhenNoUpcoming() {
        when(loanRepository.findWithUpcomingUnpaidInstallments(USER, FROM, TO)).thenReturn(List.of());
        when(cardRepository.findUpcomingUnpaidInstallments(USER, FROM, TO)).thenReturn(List.of());

        assertThat(useCase.execute(USER, FROM, TO)).isEmpty();
    }
}
