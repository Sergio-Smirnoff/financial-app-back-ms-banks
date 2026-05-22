package com.financialapp.banks.web.controller;

import com.financialapp.banks.application.card.command.*;
import com.financialapp.banks.application.card.usecase.*;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.card.CardInstallmentId;
import com.financialapp.banks.web.dto.request.CardExpenseCreateRequest;
import com.financialapp.banks.web.dto.request.CardExpenseImportRequest;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.BatchImportResponse;
import com.financialapp.banks.web.dto.response.CardInstallmentResponse;
import com.financialapp.banks.web.mapper.CardInstallmentWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/cards/{cardNumber}/installments")
@RequiredArgsConstructor
@Tag(name = "Card Installments", description = "Management of card installments and expenses")
public class CardInstallmentController {

    private final ListCardInstallmentsUseCase listCardInstallmentsUseCase;
    private final CreateCardExpenseUseCase createCardExpenseUseCase;
    private final PayCardInstallmentUseCase payCardInstallmentUseCase;
    private final ImportCardExpensesUseCase importCardExpensesUseCase;
    private final CheckDuplicateExpensesUseCase checkDuplicateExpensesUseCase;
    private final CardInstallmentWebMapper installmentMapper;

    @GetMapping
    @Operation(summary = "List installments for a card")
    public ResponseEntity<ApiResponse<List<CardInstallmentResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber) {
        List<CardInstallmentResponse> result = listCardInstallmentsUseCase
                .execute(cardNumber, new UserId(userId))
                .stream().map(installmentMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @Operation(summary = "Create an expense with installments for a card")
    public ResponseEntity<ApiResponse<List<CardInstallmentResponse>>> createExpense(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber,
            @Valid @RequestBody CardExpenseCreateRequest request) {
        Money amount = new Money(request.totalAmount(), Currency.getInstance(request.currency()));
        List<CardInstallmentResponse> result = createCardExpenseUseCase.execute(new CreateCardExpenseCommand(
                cardNumber,
                new UserId(userId),
                request.description(),
                amount,
                request.totalInstallments(),
                request.firstDueDate()
        )).stream().map(installmentMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok("Expense created", result));
    }

    @PostMapping("/{installmentId}/pay")
    @Operation(summary = "Mark an installment as paid from a specific account")
    public ResponseEntity<ApiResponse<CardInstallmentResponse>> pay(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber,
            @PathVariable Long installmentId,
            @RequestParam String accountCbu,
            @RequestParam(required = false) LocalDate paidDate) {
        var result = payCardInstallmentUseCase.execute(new PayCardInstallmentCommand(
                cardNumber,
                new CardInstallmentId(installmentId),
                new UserId(userId),
                accountCbu,
                paidDate
        ));
        return ResponseEntity.ok(ApiResponse.ok("Installment paid", installmentMapper.toResponse(result)));
    }

    @PostMapping("/import")
    @Operation(summary = "Batch import card expenses from statements")
    public ResponseEntity<ApiResponse<BatchImportResponse>> importExpenses(
            @PathVariable String cardNumber,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CardExpenseImportRequest request) {
        List<ImportCardExpensesCommand.ImportedExpense> expenses = request.expenses().stream()
                .map(e -> new ImportCardExpensesCommand.ImportedExpense(
                        e.description(),
                        new Money(e.amount(), Currency.getInstance(e.currency())),
                        e.date()))
                .toList();
        var result = importCardExpensesUseCase.execute(new ImportCardExpensesCommand(
                cardNumber,
                new UserId(userId),
                request.arsAccountCbu(),
                request.usdAccountCbu(),
                expenses
        ));
        return ResponseEntity.ok(ApiResponse.ok("Import completed",
                new BatchImportResponse(result.imported(), result.skipped(), result.errors())));
    }

    @PostMapping("/duplicates-check")
    @Operation(summary = "Check for existing card installments to avoid duplicates")
    public ResponseEntity<ApiResponse<List<Integer>>> checkDuplicates(
            @PathVariable String cardNumber,
            @RequestBody List<CardExpenseCreateRequest> expenses) {
        List<CreateCardExpenseCommand> commands = expenses.stream()
                .map(e -> new CreateCardExpenseCommand(
                        cardNumber,
                        null,
                        e.description(),
                        new Money(e.totalAmount(), Currency.getInstance(e.currency())),
                        e.totalInstallments(),
                        e.firstDueDate()
                )).toList();
        return ResponseEntity.ok(ApiResponse.ok(
                checkDuplicateExpensesUseCase.execute(cardNumber, commands)));
    }
}
