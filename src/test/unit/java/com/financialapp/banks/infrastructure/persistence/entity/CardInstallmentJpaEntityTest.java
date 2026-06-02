package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CardInstallmentJpaEntityTest {

    @Test
    void onCreate_uppercasesCurrencyAndStampsTimestamps_whenNull() {
        // Given an entity with a lowercase currency and null timestamps
        CardInstallmentJpaEntity entity = CardInstallmentJpaEntity.builder().currency("usd").build();

        // When the create hook runs
        entity.onCreate();

        // Then the currency is uppercased and timestamps are stamped
        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_keepsExistingTimestampsAndNullCurrency() {
        // Given an entity with null currency and preset timestamps (the false branches)
        LocalDateTime preset = LocalDateTime.of(2026, 1, 1, 0, 0);
        CardInstallmentJpaEntity entity = CardInstallmentJpaEntity.builder()
                .createdAt(preset).updatedAt(preset).build();

        // When the create hook runs
        entity.onCreate();

        // Then currency stays null and the preset timestamps are kept
        assertThat(entity.getCurrency()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(preset);
        assertThat(entity.getUpdatedAt()).isEqualTo(preset);
    }

    @Test
    void onUpdate_uppercasesCurrency_whenSet() {
        // Given an entity with a lowercase currency
        CardInstallmentJpaEntity entity = CardInstallmentJpaEntity.builder().currency("ars").build();

        // When the update hook runs
        entity.onUpdate();

        // Then the currency is uppercased and updatedAt stamped
        assertThat(entity.getCurrency()).isEqualTo("ARS");
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_leavesNullCurrency() {
        // Given an entity with null currency (the false branch)
        CardInstallmentJpaEntity entity = CardInstallmentJpaEntity.builder().build();

        // When the update hook runs
        entity.onUpdate();

        // Then currency stays null and updatedAt is stamped
        assertThat(entity.getCurrency()).isNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
