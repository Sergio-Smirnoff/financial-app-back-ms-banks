package com.financialapp.banks.domain.exception.cbu;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidCbuExceptionTest {

    @Test
    void carriesReasonValueAndCategory_whenValuePresent() {
        var ex = new InvalidCbuException("00700", "must be 22 digits");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_CBU);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("must be 22 digits");
        assertThat(ex.getDetails()).containsEntry("cbu", "00700");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        var ex = new InvalidCbuException(null, "missing");
        assertThat(ex.getDetails()).containsEntry("cbu", "null");
    }
}
