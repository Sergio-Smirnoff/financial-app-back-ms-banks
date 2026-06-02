package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BankJpaEntityTest {

    @Test
    void onCreate_stampsCreatedAndUpdated() {
        // Given a new bank entity
        BankJpaEntity entity = BankJpaEntity.builder().bankNumber("007").name("GALICIA").build();

        // When the create hook runs
        entity.onCreate();

        // Then both timestamps are set
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_stampsUpdated() {
        // Given a bank entity
        BankJpaEntity entity = BankJpaEntity.builder().bankNumber("007").name("GALICIA").build();

        // When the update hook runs
        entity.onUpdate();

        // Then updatedAt is set
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
