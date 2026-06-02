package com.financialapp.banks.domain.model.card;

import com.financialapp.banks.domain.exception.card.InvalidIssuerCardAccountException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IssuerCardAccountTest {

    @Test
    void constructs_whenExactly9Digits() {
        IssuerCardAccount acc = new IssuerCardAccount("000000001");
        assertThat(acc.value()).isEqualTo("000000001");
        assertThat(acc).hasToString("000000001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12345678", "1234567890", "12345678x"})
    void rejects_whenNotNineDigits(String bad) {
        assertThatThrownBy(() -> new IssuerCardAccount(bad)).isInstanceOf(InvalidIssuerCardAccountException.class);
    }
}
