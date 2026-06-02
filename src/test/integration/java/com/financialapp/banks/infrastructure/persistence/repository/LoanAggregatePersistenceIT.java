package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanAggregatePersistenceIT {

    @Autowired LoanRepository loanRepository;

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate START = LocalDate.of(2026, 6, 1);

    private Loan newLoan(BankNumber bank) {
        return Loan.originate(USER, bank, "Car loan",
                new Money(new BigDecimal("1200.00"), ARS), BigDecimal.ZERO, 12,
                AmortizationType.FRENCH, START, "0001234567890123456789").loan();
    }

    @Test
    void save_then_load_roundTripsInstallments() {
        // Given a new 12-installment loan / When saved and reloaded by id
        Loan saved = loanRepository.save(newLoan(BANK));
        Loan reloaded = loanRepository.findById(saved.id()).orElseThrow();

        // Then the schedule round-trips with generated ids
        assertThat(reloaded.installments()).hasSize(12);
        assertThat(reloaded.installments().get(0).id().value()).isNotNull();
        assertThat(reloaded.installments().get(0).amount().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void save_existingLoan_mergesInPlace() {
        // Given a persisted loan reloaded with its id / When saved again (merge branch)
        Loan saved = loanRepository.save(newLoan(BANK));
        Loan reloaded = loanRepository.findById(saved.id()).orElseThrow();

        Loan reSaved = loanRepository.save(reloaded);

        // Then it persists under the same id
        assertThat(reSaved.id().value()).isEqualTo(saved.id().value());
    }

    @Test
    void repositoryQueries_findPersistedLoan() {
        // Given a saved loan
        Loan saved = loanRepository.save(newLoan(BANK));

        // When / Then the read projections find it
        assertThat(loanRepository.findByUserId(USER)).isNotEmpty();
        assertThat(loanRepository.findByBankNumber(BANK)).isNotEmpty();
        assertThat(loanRepository.findByIdAndUserId(saved.id(), USER)).isPresent();
        assertThat(loanRepository.countByBankNumber(BANK)).isPositive();
        assertThat(loanRepository.findActiveWithUpcomingPayment(START, START.plusMonths(1))).isNotEmpty();
    }

    @Test
    void countByBankNumber_isZeroForUnknownBank() {
        // Given an unseeded bank / When counting / Then the orElse(0) branch returns 0
        assertThat(loanRepository.countByBankNumber(new BankNumber("999"))).isZero();
    }

    @Test
    void delete_removesLoan() {
        // Given a saved loan / When deleted / Then it is gone
        Loan saved = loanRepository.save(newLoan(BANK));
        loanRepository.delete(saved.id());
        assertThat(loanRepository.findById(saved.id())).isEmpty();
    }

    @Test
    void save_withNullIdObject_insertsViaToJpa() {
        // Given a loan whose id object itself is null (covers the loan.id() != null FALSE branch of save)
        Loan idless = new Loan(null, USER, BANK, "Fresh",
                new Money(new BigDecimal("100.00"), ARS), BigDecimal.ZERO, 1, 1,
                AmortizationType.FRENCH, START, true, List.of(),
                LocalDateTime.now(), LocalDateTime.now());

        // When saved / Then it is inserted with a generated id
        Loan saved = loanRepository.save(idless);
        assertThat(saved.id().value()).isNotNull();
    }

    @Test
    void save_throwsWhenBankMissing() {
        // Given a loan referencing a non-seeded bank / When saved / Then requireBank rejects it
        assertThatThrownBy(() -> loanRepository.save(newLoan(new BankNumber("999"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
