package com.financialapp.banks.domain.exception.cbu;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CbuBankMismatchExceptionTest {

    @Test
    void carriesBankAndCbuCodeAndCategory() {
        // Given / When
        var ex = new CbuBankMismatchException("007", "014");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.CBU_BANK_MISMATCH);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(ex.getMessage()).contains("007").contains("014");
        assertThat(ex.getDetails())
                .containsEntry("bankNumber", "007")
                .containsEntry("cbuEntityCode", "014");
    }
}
