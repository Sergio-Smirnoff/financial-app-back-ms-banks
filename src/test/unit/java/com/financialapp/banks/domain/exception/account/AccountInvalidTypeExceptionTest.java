package com.financialapp.banks.domain.exception.account;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountInvalidTypeExceptionTest {

    @Test
    void carriesTypeAndCategory() {
        // Given / When
        var ex = new AccountInvalidTypeException("CRYPTO");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.ACCOUNT_INVALID_TYPE);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(ex.getMessage()).contains("CRYPTO");
        assertThat(ex.getDetails()).containsEntry("subtype", "CRYPTO");
    }
}
