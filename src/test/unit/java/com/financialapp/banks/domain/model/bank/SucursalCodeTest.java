package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.InvalidSucursalCodeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SucursalCodeTest {

    @Test
    void constructs_whenExactly4Digits() {
        SucursalCode c = new SucursalCode("0001");
        assertThat(c.value()).isEqualTo("0001");
        assertThat(c).hasToString("0001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "12345", "00x1"})
    void rejects_whenNotFourDigits(String bad) {
        assertThatThrownBy(() -> new SucursalCode(bad)).isInstanceOf(InvalidSucursalCodeException.class);
    }
}
