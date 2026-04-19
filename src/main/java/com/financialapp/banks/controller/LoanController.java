package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.request.LoanRequest;
import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.model.dto.response.LoanInstallmentResponse;
import com.financialapp.banks.model.dto.response.LoanResponse;
import com.financialapp.banks.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "User loans management")
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    @Operation(summary = "List user loans, optionally filtered by account")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) Long accountId) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.list(userId, accountId)));
    }

    @PostMapping
    @Operation(summary = "Create a loan with amortized installments")
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody LoanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Loan created", loanService.create(userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        loanService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Loan deleted", null));
    }

    @GetMapping("/{id}/installments")
    @Operation(summary = "List installments for a loan")
    public ResponseEntity<ApiResponse<List<LoanInstallmentResponse>>> getInstallments(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(loanService.getInstallments(id, userId)));
    }

    @PostMapping("/{id}/installments/{installmentId}/pay")
    @Operation(summary = "Mark a loan installment as paid")
    public ResponseEntity<ApiResponse<LoanInstallmentResponse>> payInstallment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long installmentId,
            @RequestParam(required = false) LocalDate paidDate) {
        return ResponseEntity.ok(ApiResponse.ok("Installment paid",
                loanService.payInstallment(id, installmentId, userId, paidDate)));
    }
}
