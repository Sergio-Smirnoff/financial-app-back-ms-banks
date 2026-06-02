package com.financialapp.banks.domain.common.model;

import com.financialapp.banks.domain.exception.cbu.InvalidCbuException;
import com.financialapp.banks.domain.model.account.AccountNumber;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.domain.model.bank.SucursalCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CbuTest {

    private static Cbu sampleCbu() {
        return new Cbu(new BankNumber("007"), new SucursalCode("0001"), new AccountNumber("0000000000001"));
    }

    @Test
    void value_joinsPartsAndComputesBothCheckDigits() {
        // Given a CBU built from its parts
        Cbu cbu = sampleCbu();

        // When / Then value() is 22 digits and toString mirrors it
        assertThat(cbu.value()).hasSize(22).matches("\\d{22}");
        assertThat(cbu).hasToString(cbu.value());
    }

    @Test
    void exposesItsParts() {
        Cbu cbu = sampleCbu();
        assertThat(cbu.bankNumber().value()).isEqualTo("007");
        assertThat(cbu.sucursalCode().value()).isEqualTo("0001");
        assertThat(cbu.accountNumber().value()).isEqualTo("0000000000001");
    }

    @Test
    void from_roundTripsAValidCbuString() {
        // Given the canonical string produced by value() (correct check digits)
        String raw = sampleCbu().value();

        // When parsed back / Then the parts match
        Cbu parsed = Cbu.from(raw);
        assertThat(parsed.value()).isEqualTo(raw);
        assertThat(parsed.bankNumber().value()).isEqualTo("007");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "123", "00700010000000000000011", "0070001000000000000x01"})
    void from_rejectsWhenNotTwentyTwoDigits(String bad) {
        assertThatThrownBy(() -> Cbu.from(bad)).isInstanceOf(InvalidCbuException.class);
    }

    @Test
    void from_rejectsWhenCheckDigitsDoNotMatch() {
        // Given a 22-digit string whose check digits are wrong (flip the last digit)
        String valid = sampleCbu().value();
        char last = valid.charAt(21);
        String tampered = valid.substring(0, 21) + (last == '0' ? '1' : '0');

        // When / Then
        assertThatThrownBy(() -> Cbu.from(tampered))
                .isInstanceOf(InvalidCbuException.class)
                .hasMessageContaining("check digit");
    }
}
