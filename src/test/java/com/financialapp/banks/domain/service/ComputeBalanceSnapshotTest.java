package com.financialapp.banks.domain.service;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.model.snapshot.BalanceSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComputeBalanceSnapshotTest {

    private ComputeBalanceSnapshot computeService;
    private final UserId userId = new UserId(100L);
    private final LocalDate today = LocalDate.of(2026, 7, 30);
    private final BankNumber bankNumber = new BankNumber("011");

    @BeforeEach
    void setUp() {
        computeService = new ComputeBalanceSnapshot();
    }

    @Test
    void compute_emptyInputs_returnsSnapshotWithEmptyLists() {
        BalanceSnapshot snapshot = computeService.compute(userId, today, List.of(), List.of(), List.of(), List.of());

        assertThat(snapshot.userId()).isEqualTo(userId);
        assertThat(snapshot.snapshotDate()).isEqualTo(today);
        assertThat(snapshot.cashByCurrency()).isEmpty();
        assertThat(snapshot.cardDebtByCurrency()).isEmpty();
        assertThat(snapshot.loanDebtByCurrency()).isEmpty();
    }

    @Test
    void compute_activeAndInactiveAccounts_sumsOnlyActiveGroupedByCurrency() {
        Account activeArs1 = Account.create(AccountType.CHECKING, Cbu.from("0070001600000000123459"), "Acc1",
                Money.of(new BigDecimal("1000.00"), "ARS"), userId, bankNumber, "Acc1", true, LocalDateTime.now(), LocalDateTime.now());
        Account activeArs2 = Account.create(AccountType.SAVINGS, Cbu.from("0070001600000000123459"), "Acc2",
                Money.of(new BigDecimal("500.00"), "ARS"), userId, bankNumber, "Acc2", true, LocalDateTime.now(), LocalDateTime.now());
        Account inactiveArs = Account.create(AccountType.CHECKING, Cbu.from("0070001600000000123459"), "Acc3",
                Money.of(new BigDecimal("9999.00"), "ARS"), userId, bankNumber, "Acc3", false, LocalDateTime.now(), LocalDateTime.now());
        Account activeUsd = Account.create(AccountType.SAVINGS, Cbu.from("0070001600000000123459"), "Acc4",
                Money.of(new BigDecimal("250.00"), "USD"), userId, bankNumber, "Acc4", true, LocalDateTime.now(), LocalDateTime.now());

        BalanceSnapshot snapshot = computeService.compute(
                userId, today, List.of(activeArs1, activeArs2, inactiveArs, activeUsd), List.of(), List.of(), List.of());

        assertThat(snapshot.cashByCurrency()).containsExactlyInAnyOrder(
                Money.of(new BigDecimal("1500.00"), "ARS"),
                Money.of(new BigDecimal("250.00"), "USD")
        );
    }

    @Test
    void compute_cardInstallments_sumsOnlyUnpaidCurrentPeriodInstallmentsGroupedByCurrency() {
        CardInstallment unpaidArs = new CardInstallment(
                new CardInstallmentId(1L), "4500123456789012", "Phone",
                Money.of(new BigDecimal("12000.00"), "ARS"), 1, 12,
                Money.of(new BigDecimal("1000.00"), "ARS"), today, false, null, LocalDateTime.now(), LocalDateTime.now());

        CardInstallment paidArs = new CardInstallment(
                new CardInstallmentId(2L), "4500123456789012", "Phone",
                Money.of(new BigDecimal("12000.00"), "ARS"), 2, 12,
                Money.of(new BigDecimal("1000.00"), "ARS"), today, true, today, LocalDateTime.now(), LocalDateTime.now());

        CardInstallment unpaidUsd = new CardInstallment(
                new CardInstallmentId(3L), "4500123456789012", "Flight",
                Money.of(new BigDecimal("500.00"), "USD"), 1, 1,
                Money.of(new BigDecimal("500.00"), "USD"), today, false, null, LocalDateTime.now(), LocalDateTime.now());

        BalanceSnapshot snapshot = computeService.compute(
                userId, today, List.of(), List.of(), List.of(unpaidArs, paidArs, unpaidUsd), List.of());

        assertThat(snapshot.cardDebtByCurrency()).containsExactlyInAnyOrder(
                Money.of(new BigDecimal("1000.00"), "ARS"),
                Money.of(new BigDecimal("500.00"), "USD")
        );
    }

    @Test
    void compute_activeAndClosedLoans_sumsRemainingPrincipalGroupedByCurrency() {
        LoanInstallment unpaidLoanInst1 = new LoanInstallment(
                new LoanInstallmentId(10L), new LoanId(1L), 1,
                Money.of(new BigDecimal("5000.00"), "ARS"), today, false, null, LocalDateTime.now(), LocalDateTime.now());
        LoanInstallment unpaidLoanInst2 = new LoanInstallment(
                new LoanInstallmentId(11L), new LoanId(1L), 2,
                Money.of(new BigDecimal("5000.00"), "ARS"), today.plusMonths(1), false, null, LocalDateTime.now(), LocalDateTime.now());

        Loan activeLoanArs = new Loan(
                new LoanId(1L), userId, bankNumber, "Personal Loan",
                Money.of(new BigDecimal("10000.00"), "ARS"), new BigDecimal("12.00"), 2, 2,
                AmortizationType.FRENCH, today, true, List.of(unpaidLoanInst1, unpaidLoanInst2), LocalDateTime.now(), LocalDateTime.now());

        Loan closedLoanArs = new Loan(
                new LoanId(2L), userId, bankNumber, "Old Loan",
                Money.of(new BigDecimal("50000.00"), "ARS"), new BigDecimal("10.00"), 12, 0,
                AmortizationType.FRENCH, today.minusYears(1), false, List.of(), LocalDateTime.now(), LocalDateTime.now());

        BalanceSnapshot snapshot = computeService.compute(
                userId, today, List.of(), List.of(), List.of(), List.of(activeLoanArs, closedLoanArs));

        assertThat(snapshot.loanDebtByCurrency()).containsExactly(
                Money.of(new BigDecimal("10000.00"), "ARS")
        );
    }
}
