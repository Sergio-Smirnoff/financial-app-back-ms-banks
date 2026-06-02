package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidCardNumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardNumberTest {

    private static CardNumber sample() {
        return new CardNumber(new IssuerBin("450000"), new IssuerCardAccount("000000000"));
    }

    @Test
    void value_joinsPartsAndAppendsLuhnCheckDigit() {
        CardNumber pan = sample();
        assertThat(pan.value()).hasSize(16).matches("\\d{16}");
        assertThat(pan.value()).startsWith("450000000000000");
    }

    @Test
    void last4_returnsLastFourDigits() {
        CardNumber pan = sample();
        assertThat(pan.last4()).isEqualTo(pan.value().substring(12));
        assertThat(pan.last4()).hasSize(4);
    }

    @Test
    void toString_masksAllButLast4() {
        CardNumber pan = sample();
        assertThat(pan).hasToString("************" + pan.last4());
    }

    @Test
    void from_roundTripsAValidPan() {
        String raw = sample().value();
        CardNumber parsed = CardNumber.from(raw);
        assertThat(parsed.value()).isEqualTo(raw);
        assertThat(parsed.issuerBin().value()).isEqualTo("450000");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "411111111111111", "41111111111111111", "411111111111111x"})
    void from_rejectsWhenNotSixteenDigits(String bad) {
        assertThatThrownBy(() -> CardNumber.from(bad)).isInstanceOf(InvalidCardNumberException.class);
    }

    @Test
    void from_rejectsWhenLuhnCheckDigitWrong() {
        // Given a 16-digit string whose last (check) digit is wrong
        String valid = sample().value();
        char last = valid.charAt(15);
        String tampered = valid.substring(0, 15) + (last == '0' ? '1' : '0');

        assertThatThrownBy(() -> CardNumber.from(tampered)).isInstanceOf(InvalidCardNumberException.class);
    }
}
