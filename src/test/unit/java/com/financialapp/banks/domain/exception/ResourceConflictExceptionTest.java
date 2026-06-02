package com.financialapp.banks.domain.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceConflictExceptionTest {

    @Test
    void carriesErrorAndMessage_withoutDetails() {
        // Given / When
        var ex = new ResourceConflictException(DomainError.BANK_HAS_ACTIVE_ACCOUNTS, "bank has accounts");

        // Then
        assertThat(ex.getError()).isEqualTo(DomainError.BANK_HAS_ACTIVE_ACCOUNTS);
        assertThat(ex.getMessage()).isEqualTo("bank has accounts");
        assertThat(ex.getDetails()).isNull();
    }

    @Test
    void carriesDetails_whenProvided() {
        // Given / When
        var details = Map.<String, Object>of("bankNumber", "007");
        var ex = new ResourceConflictException(DomainError.BANK_HAS_ACTIVE_ACCOUNTS, "bank has accounts", details);

        // Then
        assertThat(ex.getDetails()).containsEntry("bankNumber", "007");
    }
}
