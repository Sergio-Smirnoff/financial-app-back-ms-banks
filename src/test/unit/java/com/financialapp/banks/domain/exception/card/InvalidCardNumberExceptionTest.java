package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidCardNumberExceptionTest {

    @Test
    void carriesValueAndCategory_whenValuePresent() {
        var ex = new InvalidCardNumberException("123");
        assertThat(ex.getError()).isEqualTo(DomainError.INVALID_CARD_NUMBER);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.BAD_REQUEST);
        assertThat(ex.getMessage()).contains("16 digits");
        assertThat(ex.getDetails()).containsEntry("cardNumber", "123");
    }

    @Test
    void rendersNullValueAsLiteral_whenNull() {
        var ex = new InvalidCardNumberException(null);
        assertThat(ex.getDetails()).containsEntry("cardNumber", "null");
    }
}
