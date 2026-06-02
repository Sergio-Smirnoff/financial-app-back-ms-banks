package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardJpaEntityTest {

    @Test
    void onCreate_stampsCreatedAndUpdated() {
        // Given a new card entity
        CardJpaEntity entity = CardJpaEntity.builder().cardNumber("4111111111111111").build();

        // When the create hook runs
        entity.onCreate();

        // Then both timestamps are set
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_stampsUpdated() {
        // Given a card entity
        CardJpaEntity entity = CardJpaEntity.builder().cardNumber("4111111111111111").build();

        // When the update hook runs
        entity.onUpdate();

        // Then updatedAt is set
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
