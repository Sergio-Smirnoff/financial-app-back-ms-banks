package com.financialapp.banks.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentsServiceExceptionTest {

    @Test
    void carriesErrorMessageAndDetails_whenCausePresent() {
        // Given / When
        var ex = new InvestmentsServiceException("getValuation", "503");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.INVESTMENTS_SERVICE_UNAVAILABLE);
        assertThat(ex.getError().getCategory()).isEqualTo(ErrorCategory.INTERNAL);
        assertThat(ex.getMessage()).contains("ms-investments").contains("getValuation");
        assertThat(ex.getDetails()).containsEntry("operation", "getValuation").containsEntry("cause", "503");
    }

    @Test
    void defaultsCauseToUnknown_whenNull() {
        var ex = new InvestmentsServiceException("getValuation", null);
        assertThat(ex.getDetails()).containsEntry("cause", "unknown");
    }
}
