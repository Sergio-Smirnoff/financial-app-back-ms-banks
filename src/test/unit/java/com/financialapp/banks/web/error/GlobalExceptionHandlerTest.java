package com.financialapp.banks.web.error;

import com.financialapp.banks.domain.exception.ResourceNotFoundException;
import com.financialapp.banks.web.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(new ErrorCategoryHttpMapper());
    }

    @Test
    void handleDomain_mapsCategoryCodeAndMessage() {
        // Given a domain exception (not found)
        ResponseEntity<ErrorResponse> response = handler.handleDomain(new ResourceNotFoundException("Account", "x"));

        // Then it is rendered as 404 with the domain code
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("resource_not_found");
    }

    @Test
    void handleValidation_returns400WithFieldNames() {
        // Given a binding result with one field error
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "name", "must not be blank")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When handled
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        // Then a 400 validation error is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("validation_error");
    }

    @Test
    void handleConstraintViolation_returns400() {
        // Given a constraint violation / When handled
        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(new ConstraintViolationException("bad", null));

        // Then a 400 validation error is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("validation_error");
    }

    @Test
    void handleNotReadable_returns400() {
        // Given a malformed body exception
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMostSpecificCause()).thenReturn(new RuntimeException("bad json"));

        // When handled
        ResponseEntity<ErrorResponse> response = handler.handleNotReadable(ex);

        // Then a 400 malformed_request error is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("malformed_request");
    }

    @Test
    void handleDataIntegrity_mapsKnownConstraint() {
        // Given a DB error mentioning a known constraint
        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("violates uq_banks_name unique"));

        // Then a 409 with the friendly message is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("bank with this name");
    }

    @Test
    void handleDataIntegrity_fallsBackForUnknownConstraint() {
        // Given a DB error with no recognised constraint
        ResponseEntity<ErrorResponse> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("some other failure"));

        // Then a generic data-conflict message is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Data conflict");
    }

    @Test
    void handleGeneric_returns500() {
        // Given an unexpected exception / When handled
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("boom"));

        // Then a 500 internal_error is returned
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getCode()).isEqualTo("internal_error");
    }
}
