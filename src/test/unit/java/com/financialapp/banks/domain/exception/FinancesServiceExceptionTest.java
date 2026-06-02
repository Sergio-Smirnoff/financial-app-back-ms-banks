package com.financialapp.banks.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FinancesServiceExceptionTest {

    @Test
    void carriesErrorMessageAndDetails_whenCausePresent() {
        // Given / When
        var ex = new FinancesServiceException("getTransactions", "timeout");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.FINANCES_SERVICE_UNAVAILABLE);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.INTERNAL);
        assertThat(ex.getMessage()).contains("ms-finances").contains("getTransactions");
        assertThat(ex.getDetails()).containsEntry("operation", "getTransactions").containsEntry("cause", "timeout");
    }

    @Test
    void defaultsCauseToUnknown_whenNull() {
        // Given a null cause / When / Then the ternary's null branch is taken
        var ex = new FinancesServiceException("getTransactions", null);
        assertThat(ex.getDetails()).containsEntry("cause", "unknown");
    }
}
