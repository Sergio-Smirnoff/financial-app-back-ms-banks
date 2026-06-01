package com.financialapp.banks.infrastructure.persistence.repository;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanAggregatePersistenceIT {

    @Autowired
    LoanRepository loanRepository;

    @Test
    void save_then_load_round_trips_installments() {
        Loan loan = Loan.originate(new UserId(1L), new BankNumber("007"), "Car loan",
                new Money(new BigDecimal("1200.00"), Currency.getInstance("ARS")),
                BigDecimal.ZERO, 12, AmortizationType.FRENCH, LocalDate.of(2026, 6, 1),
                "0001234567890123456789").loan();

        Loan saved = loanRepository.save(loan);

        Loan reloaded = loanRepository.findById(saved.id()).orElseThrow();
        assertThat(reloaded.installments()).hasSize(12);
        assertThat(reloaded.installments().get(0).id().value()).isNotNull();
        assertThat(reloaded.installments().get(0).amount().amount()).isEqualByComparingTo("100.00");
    }
}
