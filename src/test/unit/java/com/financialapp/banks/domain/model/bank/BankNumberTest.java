package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.InvalidBankNumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankNumberTest {

    @Test
    void constructs_whenExactly3Digits() {
        BankNumber n = new BankNumber("007");
        assertThat(n.value()).isEqualTo("007");
        assertThat(n).hasToString("007");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "12", "1234", "ab1"})
    void rejects_whenNotThreeDigits(String bad) {
        assertThatThrownBy(() -> new BankNumber(bad)).isInstanceOf(InvalidBankNumberException.class);
    }
}
