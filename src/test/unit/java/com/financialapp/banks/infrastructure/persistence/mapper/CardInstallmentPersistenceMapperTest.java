package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.card.CardInstallment;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.infrastructure.persistence.entity.CardInstallmentJpaEntity;
import com.financialapp.banks.infrastructure.persistence.entity.CardJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class CardInstallmentPersistenceMapperTest {

    private final CardInstallmentPersistenceMapper mapper = new CardInstallmentPersistenceMapper();

    private static final Currency USD = Currency.getInstance("USD");
    private static final LocalDate DUE = LocalDate.of(2026, 6, 1);
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 0, 0);

    private CardJpaEntity card() {
        return CardJpaEntity.builder().cardNumber("4111111111111111").build();
    }

    private CardInstallment domain(CardInstallmentId id) {
        Money amount = new Money(new BigDecimal("100.00"), USD);
        return new CardInstallment(id, "4111111111111111", "Mac", amount, 1, 3, amount, DUE, false, null, T0, T0);
    }

    @Test
    void toDomain_mapsEntity() {
        // Given a populated installment entity
        CardInstallmentJpaEntity entity = CardInstallmentJpaEntity.builder()
                .id(7L).card(card()).description("Mac").totalAmount(new BigDecimal("300.00")).currency("USD")
                .installmentNumber(1).totalInstallments(3).amount(new BigDecimal("100.00")).dueDate(DUE)
                .paid(false).createdAt(T0).updatedAt(T0).build();

        // When mapped to domain
        CardInstallment result = mapper.toDomain(entity);

        // Then the fields round-trip
        assertThat(result.id().value()).isEqualTo(7L);
        assertThat(result.description()).isEqualTo("Mac");
        assertThat(result.amount().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toJpa_mapsDomain_withId() {
        // Given a domain installment carrying an id
        CardInstallmentJpaEntity entity = mapper.toJpa(domain(new CardInstallmentId(7L)), card());

        // Then the id and fields are mapped
        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getCurrency()).isEqualTo("USD");
    }

    @Test
    void toJpa_mapsDomain_withNullId() {
        // Given a domain installment with a null id (the id ternary's null branch)
        CardInstallmentJpaEntity entity = mapper.toJpa(domain(new CardInstallmentId(null)), card());

        // Then the entity id is null
        assertThat(entity.getId()).isNull();
    }

    @Test
    void toJpa_mapsDomain_withNullIdObject() {
        // Given an installment whose id object itself is null (the ternary's null-object branch)
        Money amount = new Money(new BigDecimal("100.00"), USD);
        CardInstallment noId = new CardInstallment(null, "4111111111111111", "Mac", amount, 1, 3,
                amount, DUE, false, null, T0, T0);

        // When mapped to JPA / Then the entity id is null
        assertThat(mapper.toJpa(noId, card()).getId()).isNull();
    }

    @Test
    void toJpa_returnsNull_whenInstallmentNull() {
        // Given null / When mapped / Then null (the null guard)
        assertThat(mapper.toJpa(null, card())).isNull();
    }

    @Test
    void merge_updatesExistingInPlace() {
        // Given an existing entity / When merged with new values
        CardInstallmentJpaEntity existing = CardInstallmentJpaEntity.builder().id(7L).build();
        CardInstallmentJpaEntity merged = mapper.merge(existing, domain(new CardInstallmentId(7L)), card());

        // Then the same instance reflects the new state
        assertThat(merged).isSameAs(existing);
        assertThat(merged.getDescription()).isEqualTo("Mac");
        assertThat(merged.getCurrency()).isEqualTo("USD");
    }
}
