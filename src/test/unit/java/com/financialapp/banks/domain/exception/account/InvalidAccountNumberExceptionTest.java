package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidAccountNumberExceptionTest {

    @Test
    void carriesValueAndCategory_whenValuePresent() {
        var ex = new InvalidAccountNumberException("123");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_ACCOUNT_NUMBER);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("13 digits");
        assertThat(ex.getDetails()).containsEntry("accountNumber", "123");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        // exercises the value == null ? "null" : value branch
        var ex = new InvalidAccountNumberException(null);
        assertThat(ex.getDetails()).containsEntry("accountNumber", "null");
    }
}
