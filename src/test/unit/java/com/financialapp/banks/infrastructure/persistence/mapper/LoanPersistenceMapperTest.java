package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.Loan;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoanPersistenceMapperTest {

    private final LoanPersistenceMapper mapper = new LoanPersistenceMapper();

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private BankJpaEntity bank() {
        return BankJpaEntity.builder().id(1L).bankNumber("007").name("GALICIA").build();
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given a null entity / When mapped / Then null (the null guard)
        assertThat(mapper.toDomain(null, bank())).isNull();
    }

    @Test
    void toJpa_returnsNull_whenLoanNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toJpa(null, bank())).isNull();
    }

    @Test
    void toJpa_handlesNullLoanIdAndNullInstallmentIdObjects() {
        // Given a loan whose id object is null and an installment whose id object is null
        // (covers the null-object branches of both id ternaries)
        LoanInstallment noId = new LoanInstallment(null, new LoanId(null), 1,
                new Money(new BigDecimal("100.00"), ARS), DUE, false, null, T0, T0);
        Loan loan = new Loan(null, new UserId(1L), new BankNumber("007"), "Loan",
                new Money(new BigDecimal("100.00"), ARS), BigDecimal.ZERO, 1, 1,
                AmortizationType.FRENCH, DUE, true, List.of(noId), T0, T0);

        // When mapped to a JPA entity
        LoanJpaEntity entity = mapper.toJpa(loan, bank());

        // Then both the loan id and the child installment id are null
        assertThat(entity.getId()).isNull();
        assertThat(entity.getInstallments()).hasSize(1);
        assertThat(entity.getInstallments().get(0).getId()).isNull();
    }
}
