package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidIssuerBinExceptionTest {

    @Test
    void carriesValueAndCategory_whenValuePresent() {
        var ex = new InvalidIssuerBinException("12");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_ISSUER_BIN);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("6 digits");
        assertThat(ex.getDetails()).containsEntry("issuerBin", "12");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        var ex = new InvalidIssuerBinException(null);
        assertThat(ex.getDetails()).containsEntry("issuerBin", "null");
    }
}
