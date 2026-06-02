package com.financialapp.banks.domain.exception.card;

import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.exception.ErrorCategory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardExpiredExceptionTest {

    @Test
    void carriesCardNumberExpiryAndCategory() {
        // Given / When
        var ex = new CardExpiredException("4111111111111111", "2020-01");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.CARD_EXPIRED);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(ex.getMessage()).contains("4111111111111111");
        assertThat(ex.getDetails())
                .containsEntry("cardNumber", "4111111111111111")
                .containsEntry("expiryDate", "2020-01");
    }
}
