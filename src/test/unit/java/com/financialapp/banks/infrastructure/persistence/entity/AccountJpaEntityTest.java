package com.financialapp.banks.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountJpaEntityTest {

    @Test
    void onCreate_appliesDefaults_whenFieldsNull() {
        // Given an entity with null balance/isActive/currency
        AccountJpaEntity entity = AccountJpaEntity.builder().build();

        // When the create hook runs
        entity.onCreate();

        // Then defaults are applied and timestamps stamped
        assertThat(entity.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getIsActive()).isTrue();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_keepsValuesAndUppercasesCurrency_whenSet() {
        // Given an entity with all fields set (lowercase currency)
        AccountJpaEntity entity = AccountJpaEntity.builder()
                .balance(new BigDecimal("100.00")).isActive(false).currency("ars").build();

        // When the create hook runs
        entity.onCreate();

        // Then values are kept and the currency uppercased
        assertThat(entity.getBalance()).isEqualByComparingTo("100.00");
        assertThat(entity.getIsActive()).isFalse();
        assertThat(entity.getCurrency()).isEqualTo("ARS");
    }

    @Test
    void onUpdate_uppercasesCurrency_whenSet() {
        // Given an entity with a lowercase currency
        AccountJpaEntity entity = AccountJpaEntity.builder().currency("usd").build();

        // When the update hook runs
        entity.onUpdate();

        // Then the currency is uppercased and updatedAt stamped
        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_leavesNullCurrency() {
        // Given an entity with null currency (the false branch)
        AccountJpaEntity entity = AccountJpaEntity.builder().build();

        // When the update hook runs
        entity.onUpdate();

        // Then currency stays null and updatedAt is stamped
        assertThat(entity.getCurrency()).isNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
