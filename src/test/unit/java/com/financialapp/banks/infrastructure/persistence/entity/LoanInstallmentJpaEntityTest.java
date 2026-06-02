package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LoanInstallmentJpaEntityTest {

    @Test
    void onCreate_stampsTimestamps_whenNull() {
        // Given an entity with null timestamps
        LoanInstallmentJpaEntity entity = LoanInstallmentJpaEntity.builder().build();

        // When the create hook runs
        entity.onCreate();

        // Then both timestamps are stamped
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_keepsPresetTimestamps() {
        // Given an entity with preset timestamps (the false branches)
        LocalDateTime preset = LocalDateTime.of(2026, 1, 1, 0, 0);
        LoanInstallmentJpaEntity entity = LoanInstallmentJpaEntity.builder()
                .createdAt(preset).updatedAt(preset).build();

        // When the create hook runs
        entity.onCreate();

        // Then the preset timestamps are kept
        assertThat(entity.getCreatedAt()).isEqualTo(preset);
        assertThat(entity.getUpdatedAt()).isEqualTo(preset);
    }

    @Test
    void onUpdate_stampsUpdated() {
        // Given an installment entity
        LoanInstallmentJpaEntity entity = LoanInstallmentJpaEntity.builder().build();

        // When the update hook runs
        entity.onUpdate();

        // Then updatedAt is stamped
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
