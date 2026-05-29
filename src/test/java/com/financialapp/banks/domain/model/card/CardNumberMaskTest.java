package com.financialapp.banks.domain.model.card;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberMaskTest {

    @Test
    void toString_masks_all_but_last_four_digits() {
        CardNumber number = new CardNumber("1234567890123456");
        assertThat(number.toString()).isEqualTo("************3456");
        assertThat(number.toString()).doesNotContain("123456789012");
    }

    @Test
    void value_still_returns_the_full_number() {
        CardNumber number = new CardNumber("1234567890123456");
        assertThat(number.value()).isEqualTo("1234567890123456");
    }
}
