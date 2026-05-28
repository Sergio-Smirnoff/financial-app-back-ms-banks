package com.financialapp.banks.web.error;

import com.financialapp.banks.domain.exception.DomainException;
import com.financialapp.banks.web.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
        "uq_accounts_bank_name", "An account with this name already exists in the selected bank",
        "uq_banks_name", "A bank with this name already exists",
        "idx_cards_card_number", "A card with this number already exists"
    );

    private final ErrorCategoryHttpMapper httpMapper;

    public GlobalExceptionHandler(ErrorCategoryHttpMapper httpMapper) {
        this.httpMapper = httpMapper;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        log.warn("Domain error [{}]: {}", ex.getError().getCode(), ex.getMessage());
        HttpStatus status = httpMapper.toHttpStatus(ex.getError().getCategory());
        ErrorResponse body = ErrorResponse.builder()
            .status(status.value())
            .code(ex.getError().getCode())
            .message(ex.getMessage())
            .details(ex.getDetails())
            .build();
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fields = ex.getBindingResult().getFieldErrors()
            .stream().map(FieldError::getField).toList();
        ErrorResponse body = ErrorResponse.builder()
            .status(400).code("validation_error")
            .message("Request validation failed")
            .details(Map.of("fields", fields))
            .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse body = ErrorResponse.builder()
            .status(400).code("validation_error")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMostSpecificCause().getMessage());
        ErrorResponse body = ErrorResponse.builder()
            .status(400).code("malformed_request")
            .message("Malformed or invalid request body")
            .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String cause = ex.getMostSpecificCause().getMessage();
        String constraint = CONSTRAINT_MESSAGES.keySet().stream()
            .filter(k -> cause != null && cause.contains(k))
            .findFirst().orElse("unknown_constraint");
        String message = CONSTRAINT_MESSAGES.getOrDefault(constraint, "Data conflict");
        ErrorResponse body = ErrorResponse.builder()
            .status(409).code("database_conflict")
            .message(message)
            .details(Map.of("constraint", constraint))
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse body = ErrorResponse.builder()
            .status(500).code("internal_error")
            .message("An unexpected error occurred")
            .build();
        return ResponseEntity.internalServerError().body(body);
    }
}
