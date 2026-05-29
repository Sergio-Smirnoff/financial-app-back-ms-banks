package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.loan.command.*;
import com.financialapp.banks.domain.usecase.loan.*;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.loan.AmortizationType;
import com.financialapp.banks.domain.model.loan.LoanId;
import com.financialapp.banks.domain.model.loan.LoanInstallmentId;
import com.financialapp.banks.web.dto.request.LoanRequest;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.LoanInstallmentResponse;
import com.financialapp.banks.web.dto.response.LoanResponse;
import com.financialapp.banks.web.mapper.LoanInstallmentWebMapper;
import com.financialapp.banks.web.mapper.LoanWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "User loans management")
public class LoanController {

    private final ListLoansUseCase listLoansUseCase;
    private final CreateLoanUseCase createLoanUseCase;
    private final DeleteLoanUseCase deleteLoanUseCase;
    private final GetLoanInstallmentsUseCase getLoanInstallmentsUseCase;
    private final PayLoanInstallmentUseCase payLoanInstallmentUseCase;
    private final LoanWebMapper loanMapper;
    private final LoanInstallmentWebMapper installmentMapper;

    @GetMapping
    @Operation(summary = "List user loans, optionally filtered by bank")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String bankName) {
        BankName bank = bankName != null ? BankName.fromString(bankName) : null;
        List<LoanResponse> result = listLoansUseCase.execute(new UserId(userId), bank)
                .stream().map(loanMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @Operation(summary = "Create a loan with amortized installments")
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody LoanRequest request) {
        var loan = createLoanUseCase.execute(new CreateLoanCommand(
                new UserId(userId),
                BankName.fromString(request.bankName()),
                request.destinationAccountCbu(),
                request.name(),
                new BigDecimal(request.principal()),
                new BigDecimal(request.interestRate()),
                request.totalInstallments(),
                request.startDate(),
                AmortizationType.FRENCH
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Loan created", loanMapper.toResponse(loan)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        deleteLoanUseCase.execute(new DeleteLoanCommand(new LoanId(id), new UserId(userId)));
        return ResponseEntity.ok(ApiResponse.ok("Loan deleted", null));
    }

    @GetMapping("/{id}/installments")
    @Operation(summary = "List installments for a loan")
    public ResponseEntity<ApiResponse<List<LoanInstallmentResponse>>> getInstallments(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        List<LoanInstallmentResponse> result = getLoanInstallmentsUseCase
                .execute(new LoanId(id), new UserId(userId))
                .stream().map(installmentMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{id}/installments/{installmentId}/pay")
    @Operation(summary = "Mark a loan installment as paid from a specific account")
    public ResponseEntity<ApiResponse<LoanInstallmentResponse>> payInstallment(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long installmentId,
            @RequestParam String accountCbu,
            @RequestParam(required = false) LocalDate paidDate) {
        var result = payLoanInstallmentUseCase.execute(new PayLoanInstallmentCommand(
                new LoanId(id),
                new LoanInstallmentId(installmentId),
                new UserId(userId),
                accountCbu,
                paidDate
        ));
        return ResponseEntity.ok(ApiResponse.ok("Installment paid", installmentMapper.toResponse(result)));
    }
}
