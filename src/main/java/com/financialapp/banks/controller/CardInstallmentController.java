package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.request.CardExpenseCreateRequest;
import com.financialapp.banks.model.dto.request.CardExpenseImportRequest;
import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.model.dto.response.BatchImportResponse;
import com.financialapp.banks.model.dto.response.CardInstallmentResponse;
import com.financialapp.banks.service.CardInstallmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/cards/{cardId}/installments")
@RequiredArgsConstructor
@Tag(name = "Card Installments", description = "Management of card installments and expenses")
public class CardInstallmentController {

    private final CardInstallmentService installmentService;

    @GetMapping
    @Operation(summary = "List installments for a card")
    public ResponseEntity<ApiResponse<List<CardInstallmentResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId) {
        return ResponseEntity.ok(ApiResponse.ok(installmentService.listByCard(cardId, userId)));
    }

    @PostMapping
    @Operation(summary = "Create an expense with installments for a card")
    public ResponseEntity<ApiResponse<List<CardInstallmentResponse>>> createExpense(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody CardExpenseCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Expense created", installmentService.createExpense(cardId, userId, request)));
    }

    @PostMapping("/{installmentId}/pay")
    @Operation(summary = "Mark an installment as paid from a specific account")
    public ResponseEntity<ApiResponse<CardInstallmentResponse>> pay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId,
            @PathVariable Long installmentId,
            @RequestParam Long accountId,
            @RequestParam(required = false) LocalDate paidDate) {
        return ResponseEntity.ok(ApiResponse.ok("Installment paid",
                installmentService.payInstallment(cardId, installmentId, userId, accountId, paidDate, false)));
    }

    @PostMapping("/import")
    @Operation(summary = "Batch import card expenses from statements")
    public ResponseEntity<ApiResponse<BatchImportResponse>> importExpenses(
            @PathVariable Long cardId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "true") boolean bypassBalance,
            @RequestBody CardExpenseImportRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Import completed", installmentService.importExpenses(cardId, userId, request, bypassBalance)));
    }

    @PostMapping("/duplicates-check")
    @Operation(summary = "Check for existing card installments to avoid duplicates")
    public ResponseEntity<ApiResponse<List<Integer>>> checkDuplicates(
            @PathVariable Long cardId,
            @RequestBody List<CardExpenseCreateRequest> expenses) {
        return ResponseEntity.ok(ApiResponse.ok(installmentService.checkDuplicates(cardId, expenses)));
    }
}
