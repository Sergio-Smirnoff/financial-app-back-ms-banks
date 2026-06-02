package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardInvalidTypeExceptionTest {

    @Test
    void carriesTypeAndCategory() {
        // Given / When
        var ex = new CardInvalidTypeException("PREPAID");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.CARD_INVALID_TYPE);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(ex.getMessage()).contains("PREPAID");
        assertThat(ex.getDetails()).containsEntry("subtype", "PREPAID");
    }
}
