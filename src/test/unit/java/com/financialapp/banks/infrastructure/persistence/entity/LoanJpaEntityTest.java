package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanJpaEntityTest {

    @Test
    void onCreate_uppercasesCurrencyAndStampsTimestamps_whenSet() {
        // Given a loan entity with a lowercase currency
        LoanJpaEntity entity = LoanJpaEntity.builder().currency("ars").build();

        // When the create hook runs
        entity.onCreate();

        // Then the currency is uppercased and timestamps stamped
        assertThat(entity.getCurrency()).isEqualTo("ARS");
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_leavesNullCurrency() {
        // Given a loan entity with null currency (the false branch)
        LoanJpaEntity entity = LoanJpaEntity.builder().build();

        // When the create hook runs
        entity.onCreate();

        // Then currency stays null but timestamps are stamped
        assertThat(entity.getCurrency()).isNull();
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void onUpdate_uppercasesCurrency_whenSet() {
        // Given a loan entity with a lowercase currency
        LoanJpaEntity entity = LoanJpaEntity.builder().currency("usd").build();

        // When the update hook runs
        entity.onUpdate();

        // Then the currency is uppercased and updatedAt stamped
        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_leavesNullCurrency() {
        // Given a loan entity with null currency (the false branch)
        LoanJpaEntity entity = LoanJpaEntity.builder().build();

        // When the update hook runs
        entity.onUpdate();

        // Then currency stays null and updatedAt is stamped
        assertThat(entity.getCurrency()).isNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
