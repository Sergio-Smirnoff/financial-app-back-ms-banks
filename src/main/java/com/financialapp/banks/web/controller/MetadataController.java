package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.catalog.GetBankingCatalogUseCase;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.BankingCatalogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/banks/metadata")
@RequiredArgsConstructor
@Tag(name = "Metadata", description = "Read-only catalog of valid enum values for accounts and cards")
public class MetadataController {

    private final GetBankingCatalogUseCase getBankingCatalogUseCase;

    @GetMapping
    @Operation(summary = "List valid account/card enum values for form selectors")
    public ResponseEntity<ApiResponse<BankingCatalogResponse>> catalog() {
        return ResponseEntity.ok(ApiResponse.ok(getBankingCatalogUseCase.execute()));
    }
}
