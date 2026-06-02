package com.financialapp.banks.infrastructure.persistence.mapper;

import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.infrastructure.persistence.entity.BankJpaEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankPersistenceMapperTest {

    private final BankPersistenceMapper mapper = new BankPersistenceMapper();

    @Test
    void toDomain_mapsEntityFields() {
        // Given a bank entity
        BankJpaEntity entity = BankJpaEntity.builder().bankNumber("007").name("GALICIA").build();

        // When mapped to domain
        Bank bank = mapper.toDomain(entity);

        // Then the bank number and name are carried over
        assertThat(bank.bankNumber().value()).isEqualTo("007");
        assertThat(bank.name()).isEqualTo("GALICIA");
    }

    @Test
    void toDomain_returnsNull_whenEntityNull() {
        // Given a null entity / When mapped / Then null is returned (the null guard)
        assertThat(mapper.toDomain(null)).isNull();
    }
}
