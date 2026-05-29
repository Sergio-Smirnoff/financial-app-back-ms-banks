package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class LoanOriginationTest {

    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void originate_builds_loan_with_full_schedule() {
        Loan loan = Loan.originate(
                new UserId(1L), BankName.GALICIA, "Car loan",
                new Money(new BigDecimal("12000.00"), ARS),
                new BigDecimal("0"), 12, AmortizationType.FRENCH,
                LocalDate.of(2026, 6, 1), "0001234567890123456789").loan();

        assertThat(loan.id().value()).isNull();
        assertThat(loan.active()).isTrue();
        assertThat(loan.remainingInstallments()).isEqualTo(12);
        assertThat(loan.installments()).hasSize(12);
        assertThat(loan.installments().get(0).installmentNumber()).isEqualTo(1);
        assertThat(loan.installments().get(0).dueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(loan.installments().get(11).dueDate()).isEqualTo(LocalDate.of(2027, 5, 1));
        assertThat(loan.installments().get(0).amount().amount()).isEqualByComparingTo("1000.00");
    }
}
