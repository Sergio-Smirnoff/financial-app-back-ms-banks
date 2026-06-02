package com.financialapp.banks.domain.model.loan;

import com.financialapp.banks.domain.common.model.Cbu;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.Account;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankNumber;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRequestTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 0, 0);
    private static final LocalDate START = LocalDate.of(2026, 6, 1);

    private Account account() {
        return Account.create(AccountType.CHECKING, Cbu.from("0070001600000000123459"), "alias",
                new Money(new BigDecimal("100.00"), ARS), new UserId(1L), new BankNumber("007"),
                "My Account", true, NOW, NOW);
    }

    private Loan loan() {
        return new Loan(new LoanId(1L), new UserId(1L), new BankNumber("007"), "Loan",
                new Money(new BigDecimal("1000.00"), ARS), BigDecimal.ZERO, 1, 1,
                AmortizationType.FRENCH, START, true, List.of(), NOW, NOW);
    }

    @Test
    void accessors_returnConstructorValues() {
        // Given
        LoanRequestId id = new LoanRequestId(5L);
        Loan loan = loan();
        Account account = account();

        // When
        LoanRequest request = new LoanRequest(id, loan, NOW, NOW, START, account);

        // Then
        assertThat(request.id()).isEqualTo(id);
        assertThat(request.Loan()).isEqualTo(loan);
        assertThat(request.createdAt()).isEqualTo(NOW);
        assertThat(request.updatedAt()).isEqualTo(NOW);
        assertThat(request.startDate()).isEqualTo(START);
        assertThat(request.depositAccount()).isEqualTo(account);
    }

    @Test
    void equalsAndHashCode_areValueBased() {
        // Given
        LoanRequestId id = new LoanRequestId(5L);
        Loan loan = loan();
        Account account = account();
        LoanRequest one = new LoanRequest(id, loan, NOW, NOW, START, account);
        LoanRequest same = new LoanRequest(id, loan, NOW, NOW, START, account);
        LoanRequest other = new LoanRequest(new LoanRequestId(6L), loan, NOW, NOW, START, account);

        // Then
        assertThat(one)
                .isEqualTo(same)
                .hasSameHashCodeAs(same)
                .isNotEqualTo(other)
                .isNotEqualTo(null)
                .isNotEqualTo("x");
    }

    @Test
    void toString_includesComponents() {
        // Given
        LoanRequest request = new LoanRequest(new LoanRequestId(5L), loan(), NOW, NOW, START, account());

        // When / Then
        assertThat(request.toString()).contains("LoanRequest");
    }
}
