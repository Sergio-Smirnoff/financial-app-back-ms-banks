package com.financialapp.banks.web.error;

import com.financialapp.commons.web.error.ApiExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ApiExceptionHandler {

    @Override
    protected Map<String, String> constraintMessages() {
        return Map.of(
            "uq_accounts_bank_name", "An account with this name already exists in the selected bank",
            "uq_banks_name", "A bank with this name already exists",
            "idx_cards_card_number", "A card with this number already exists"
        );
    }
}
