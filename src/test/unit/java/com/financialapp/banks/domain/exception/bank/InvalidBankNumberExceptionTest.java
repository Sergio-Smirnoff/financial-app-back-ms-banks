package com.financialapp.banks.domain.exception.bank;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidBankNumberExceptionTest {

    @Test
    void carriesValueAndCategory_whenValuePresent() {
        var ex = new InvalidBankNumberException("12");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_BANK_NUMBER);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("3 digits");
        assertThat(ex.getDetails()).containsEntry("bankNumber", "12");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        var ex = new InvalidBankNumberException(null);
        assertThat(ex.getDetails()).containsEntry("bankNumber", "null");
    }
}
