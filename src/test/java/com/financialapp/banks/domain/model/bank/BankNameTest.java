package com.financialapp.banks.domain.model.bank;

import com.financialapp.banks.domain.exception.bank.UnsupportedBankException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BankNameTest {

    @Test
    void fromString_validName_returnsEnum() {
        assertThat(BankName.fromString("GALICIA")).isEqualTo(BankName.GALICIA);
    }

    @Test
    void fromString_invalidName_throwsUnsupportedBank() {
        assertThatThrownBy(() -> BankName.fromString("NOT_A_BANK"))
                .isInstanceOf(UnsupportedBankException.class);
    }

    @Test
    void logoUrl_isNullForNow() {
        assertThat(BankName.GALICIA.getLogoUrl()).isNull();
    }
}
