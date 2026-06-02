package com.financialapp.banks.domain.exception.bank;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidSucursalCodeExceptionTest {

    @Test
    void carriesValueAndCategory_whenValuePresent() {
        var ex = new InvalidSucursalCodeException("12");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_SUCURSAL_CODE);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("4 digits");
        assertThat(ex.getDetails()).containsEntry("sucursalCode", "12");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        var ex = new InvalidSucursalCodeException(null);
        assertThat(ex.getDetails()).containsEntry("sucursalCode", "null");
    }
}
