package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.loan.LoanInstallment;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.infrastructure.persistence.entity.LoanInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.LoanJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class LoanInstallmentPersistenceMapperTest {

    private final LoanInstallmentPersistenceMapper mapper = new LoanInstallmentPersistenceMapper();

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private LoanJpaEntity loan() {
        return LoanJpaEntity.builder().id(5L).currency("ARS").build();
    }

    private LoanInstallment domain(LoanInstallmentId id) {
        return new LoanInstallment(id, new LoanId(5L), 1, new Money(new BigDecimal("100.00"), ARS),
                DUE, false, null, T0, T0);
    }

    @Test
    void toDomain_mapsEntity() {
        // Given a populated installment entity with its parent loan
        LoanInstallmentJpaEntity entity = LoanInstallmentJpaEntity.builder()
                .id(9L).loan(loan()).installmentNumber(1).amount(new BigDecimal("100.00")).dueDate(DUE)
                .paid(false).createdAt(T0).updatedAt(T0).build();

        // When mapped to domain
        LoanInstallment result = mapper.toDomain(entity);

        // Then the fields round-trip (currency comes from the parent loan)
        assertThat(result.id().value()).isEqualTo(9L);
        assertThat(result.amount().currency()).isEqualTo(ARS);
        assertThat(result.installmentNumber()).isEqualTo(1);
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toJpa_mapsDomain_withId() {
        // Given a domain installment carrying an id
        LoanInstallmentJpaEntity entity = mapper.toJpa(domain(new LoanInstallmentId(9L)), loan());

        // Then id and fields are mapped
        assertThat(entity.getId()).isEqualTo(9L);
        assertThat(entity.getInstallmentNumber()).isEqualTo(1);
    }

    @Test
    void toJpa_mapsDomain_withNullId() {
        // Given a domain installment with null id (the id ternary's null branch)
        LoanInstallmentJpaEntity entity = mapper.toJpa(domain(new LoanInstallmentId(null)), loan());

        // Then the entity id is null
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toJpa_mapsDomain_withNullIdObject() {
        // Given an installment whose id object itself is null (the ternary's null-object branch)
        LoanInstallment noId = new LoanInstallment(null, new LoanId(5L), 1,
                new Money(new BigDecimal("100.00"), ARS), DUE, false, null, T0, T0);

        // When mapped to JPA / Then the entity id is null
        assertThat(mapper.toJpa(noId, loan()).getId()).isNull();
    }

    @Test
    void toJpa_returnsNull_whenInstallmentNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toJpa(null, loan())).isNull();
    }

    @Test
    void merge_updatesExistingInPlace() {
        // Given an existing entity / When merged with new values
        LoanInstallmentJpaEntity existing = LoanInstallmentJpaEntity.builder().id(9L).build();
        LoanInstallmentJpaEntity merged = mapper.merge(existing, domain(new LoanInstallmentId(9L)), loan());

        // Then the same instance reflects the new state
        assertThat(merged).isSameAs(existing);
        assertThat(merged.getInstallmentNumber()).isEqualTo(1);
        assertThat(merged.getAmount()).isEqualByComparingTo("100.00");
    }
}
