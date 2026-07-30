package com.financialapp.banks.domain.exception;

import com.financialapp.commons.core.error.ErrorCategory;
import com.financialapp.commons.core.error.ErrorCode;

public enum DomainError implements ErrorCode {

    // --- Resource lifecycle ---
    RESOURCE_NOT_FOUND(ErrorCategory.NOT_FOUND, "resource_not_found"),

    RESOURCE_ALREADY_EXISTS(ErrorCategory.CONFLICT, "resource_already_exists"),

    // --- Deletion conflicts ---
    BANK_HAS_ACTIVE_ACCOUNTS(ErrorCategory.CONFLICT, "bank_has_active_accounts"),
    ACCOUNT_NOT_DELETABLE(ErrorCategory.CONFLICT, "account_not_deletable"),
    CARD_NOT_DELETABLE(ErrorCategory.CONFLICT, "card_not_deletable"),

    // --- Account invariants ---
    ACCOUNT_INSUFFICIENT_FUNDS(ErrorCategory.UNPROCESSABLE, "account_insufficient_funds"),
    ACCOUNT_CURRENCY_MISMATCH(ErrorCategory.UNPROCESSABLE, "account_currency_mismatch"),

    // --- Card invariants ---
    CARD_EXPIRED(ErrorCategory.UNPROCESSABLE, "card_expired"),
    CARD_INSTALLMENT_ALREADY_PAID(ErrorCategory.CONFLICT, "card_installment_already_paid"),
    CARD_INSTALLMENT_MISMATCH(ErrorCategory.UNPROCESSABLE, "card_installment_mismatch"),
    CARD_INSTALLMENT_NOT_SUPPORTED(ErrorCategory.UNPROCESSABLE, "card_installment_not_supported"),
    CARD_INVALID_TYPE(ErrorCategory.UNPROCESSABLE, "card_invalid_type"),

    // --- Loan invariants ---
    LOAN_ALREADY_CLOSED(ErrorCategory.CONFLICT, "loan_already_closed"),
    LOAN_ACCOUNT_MISMATCH(ErrorCategory.UNPROCESSABLE, "loan_account_mismatch"),
    LOAN_INSTALLMENT_ALREADY_PAID(ErrorCategory.CONFLICT, "loan_installment_already_paid"),
    LOAN_INSTALLMENT_MISMATCH(ErrorCategory.UNPROCESSABLE, "loan_installment_mismatch"),

    // --- Input validation ---
    INVALID_DATE_RANGE(ErrorCategory.BAD_REQUEST, "invalid_date_range"),
    UNSUPPORTED_BANK(ErrorCategory.BAD_REQUEST, "unsupported_bank"),
    INVALID_CURRENCY(ErrorCategory.BAD_REQUEST, "invalid_currency"),
    INVALID_CARD_NUMBER(ErrorCategory.BAD_REQUEST, "invalid_card_number"),
    INVALID_CARD_CHECK_DIGIT(ErrorCategory.BAD_REQUEST, "invalid_card_check_digit"),
    INVALID_ISSUER_BIN(ErrorCategory.BAD_REQUEST, "invalid_issuer_bin"),
    INVALID_ISSUER_CARD_ACCOUNT(ErrorCategory.BAD_REQUEST, "invalid_issuer_card_account"),
    INVALID_BANK_NUMBER(ErrorCategory.BAD_REQUEST, "invalid_bank_number"),
    INVALID_SUCURSAL_CODE(ErrorCategory.BAD_REQUEST, "invalid_sucursal_code"),
    INVALID_ACCOUNT_NUMBER(ErrorCategory.BAD_REQUEST, "invalid_account_number"),
    INVALID_CBU(ErrorCategory.BAD_REQUEST, "invalid_cbu"),
    CBU_BANK_MISMATCH(ErrorCategory.UNPROCESSABLE, "cbu_bank_mismatch"),
    INVALID_BALANCE_SNAPSHOT(ErrorCategory.BAD_REQUEST, "invalid_balance_snapshot"),

    // --- Downstream / infrastructure ---
    FINANCES_SERVICE_UNAVAILABLE(ErrorCategory.INTERNAL_SERVER_ERROR, "finances_service_unavailable"),
    INTERNAL_ERROR(ErrorCategory.INTERNAL_SERVER_ERROR, "internal_error");

    private final ErrorCategory category;
    private final String code;

    DomainError(ErrorCategory category, String code) {
        this.category = category;
        this.code = code;
    }

    @Override
    public ErrorCategory category() { return category; }

    @Override
    public String code() { return code; }
}
