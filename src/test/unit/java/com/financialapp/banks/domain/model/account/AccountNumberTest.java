package com.financialapp.banks.domain.model.account;

import com.financialapp.banks.domain.exception.account.InvalidAccountNumberException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountNumberTest {

    @Test
    void constructs_whenExactly13Digits() {
        AccountNumber n = new AccountNumber("0000000000001");
        assertThat(n.value()).isEqualTo("0000000000001");
        assertThat(n).hasToString("0000000000001");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "00000000000012", "000000000000x"})
    void rejects_whenNotThirteenDigits(String bad) {
        assertThatThrownBy(() -> new AccountNumber(bad)).isInstanceOf(InvalidAccountNumberException.class);
    }
}
