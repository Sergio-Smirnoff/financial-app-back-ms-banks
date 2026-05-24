package com.financialapp.banks.domain.exception;

import org.springframework.http.HttpStatus;

public enum DomainError {

    // Generic not-found (replaces 6 specific not-found exceptions)
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "resource_not_found"),

    // Generic already-exists (replaces 4 specific already-exists exceptions)
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "resource_already_exists"),

    // Conflict: resource has dependencies preventing deletion
    BANK_HAS_ACTIVE_ACCOUNTS(HttpStatus.CONFLICT, "bank_has_active_accounts"),
    ACCOUNT_NOT_DELETABLE(HttpStatus.CONFLICT, "account_not_deletable"),
    CARD_NOT_DELETABLE(HttpStatus.CONFLICT, "card_not_deletable"),

    // Account business rules
    ACCOUNT_INSUFFICIENT_FUNDS(HttpStatus.UNPROCESSABLE_ENTITY, "account_insufficient_funds"),
    ACCOUNT_CURRENCY_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "account_currency_mismatch"),
    ACCOUNT_INVESTMENT_RESTRICTION(HttpStatus.UNPROCESSABLE_ENTITY, "account_investment_restriction"),
    ACCOUNT_INVALID_TYPE(HttpStatus.UNPROCESSABLE_ENTITY, "account_invalid_type"),

    // Card business rules
    CARD_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, "card_expired"),
    CARD_INSTALLMENT_ALREADY_PAID(HttpStatus.CONFLICT, "card_installment_already_paid"),
    CARD_INSTALLMENT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "card_installment_mismatch"),
    CARD_INSTALLMENT_NOT_SUPPORTED(HttpStatus.UNPROCESSABLE_ENTITY, "card_installment_not_supported"),
    CARD_INVALID_TYPE(HttpStatus.UNPROCESSABLE_ENTITY, "card_invalid_type"),

    // Loan business rules
    LOAN_ALREADY_CLOSED(HttpStatus.CONFLICT, "loan_already_closed"),
    LOAN_ACCOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "loan_account_mismatch"),
    LOAN_INSTALLMENT_ALREADY_PAID(HttpStatus.CONFLICT, "loan_installment_already_paid"),
    LOAN_INSTALLMENT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "loan_installment_mismatch"),

    // Request validation
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "invalid_date_range"),

    // Infrastructure
    FINANCES_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "finances_service_unavailable"),
    INVESTMENTS_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "investments_service_unavailable"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error");

    private final HttpStatus httpStatus;
    private final String code;

    DomainError(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
}
