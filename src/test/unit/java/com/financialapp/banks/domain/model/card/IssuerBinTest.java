package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidIssuerBinException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IssuerBinTest {

    @Test
    void constructs_whenExactly6Digits() {
        IssuerBin bin = new IssuerBin("450000");
        assertThat(bin.value()).isEqualTo("450000");
        assertThat(bin).hasToString("450000");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12345", "1234567", "12345x"})
    void rejects_whenNotSixDigits(String bad) {
        assertThatThrownBy(() -> new IssuerBin(bad)).isInstanceOf(InvalidIssuerBinException.class);
    }
}
