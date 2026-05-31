package com.financialapp.banks.domain.model.card;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberMaskTest {

    @Test
    void toString_masks_all_but_last_four_digits() {
        CardNumber number = CardNumber.from("4111111111111111");
        assertThat(number.toString()).isEqualTo("************1111");
        assertThat(number.toString()).doesNotContain("411111111111");
    }

    @Test
    void value_still_returns_the_full_number() {
        CardNumber number = CardNumber.from("4111111111111111");
        assertThat(number.value()).isEqualTo("4111111111111111");
    }
}
