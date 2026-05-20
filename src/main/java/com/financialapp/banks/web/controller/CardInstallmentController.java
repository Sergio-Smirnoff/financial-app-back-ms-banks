package com.financialapp.banks.web.controller;

import com.financialapp.banks.application.card.command.*;
import com.financialapp.banks.application.card.usecase.*;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.card.CardId;
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
@RequestMapping("/api/v1/banks/cards/{cardId}/installments")
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
            @PathVariable Long cardId) {
        List<CardInstallmentResponse> result = listCardInstallmentsUseCase
                .execute(new CardId(cardId), new UserId(userId))
                .stream().map(installmentMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    @Operation(summary = "Create an expense with installments for a card")
    public ResponseEntity<ApiResponse<List<CardInstallmentResponse>>> createExpense(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId,
            @Valid @RequestBody CardExpenseCreateRequest request) {
        Money amount = new Money(request.totalAmount(), Currency.getInstance(request.currency()));
        List<CardInstallmentResponse> result = createCardExpenseUseCase.execute(new CreateCardExpenseCommand(
                new CardId(cardId),
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
            @PathVariable Long cardId,
            @PathVariable Long installmentId,
            @RequestParam Long accountId,
            @RequestParam(required = false) LocalDate paidDate) {
        var result = payCardInstallmentUseCase.execute(new PayCardInstallmentCommand(
                new CardId(cardId),
                new CardInstallmentId(installmentId),
                new UserId(userId),
                new AccountId(accountId),
                paidDate
        ));
        return ResponseEntity.ok(ApiResponse.ok("Installment paid", installmentMapper.toResponse(result)));
    }

    @PostMapping("/import")
    @Operation(summary = "Batch import card expenses from statements")
    public ResponseEntity<ApiResponse<BatchImportResponse>> importExpenses(
            @PathVariable Long cardId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "true") boolean bypassBalance,
            @RequestBody CardExpenseImportRequest request) {
        List<ImportCardExpensesCommand.ImportedExpense> expenses = request.expenses().stream()
                .map(e -> new ImportCardExpensesCommand.ImportedExpense(
                        e.description(),
                        new Money(e.amount(), Currency.getInstance(e.currency())),
                        e.date()))
                .toList();
        AccountId usdAccountId = request.usdAccountId() != null ? new AccountId(request.usdAccountId()) : null;
        var result = importCardExpensesUseCase.execute(new ImportCardExpensesCommand(
                new CardId(cardId),
                new UserId(userId),
                new AccountId(request.arsAccountId()),
                usdAccountId,
                expenses
        ));
        return ResponseEntity.ok(ApiResponse.ok("Import completed",
                new BatchImportResponse(result.imported(), result.skipped(), result.errors())));
    }

    @PostMapping("/duplicates-check")
    @Operation(summary = "Check for existing card installments to avoid duplicates")
    public ResponseEntity<ApiResponse<List<Integer>>> checkDuplicates(
            @PathVariable Long cardId,
            @RequestBody List<CardExpenseCreateRequest> expenses) {
        List<CreateCardExpenseCommand> commands = expenses.stream()
                .map(e -> new CreateCardExpenseCommand(
                        new CardId(cardId),
                        null,
                        e.description(),
                        new Money(e.totalAmount(), Currency.getInstance(e.currency())),
                        e.totalInstallments(),
                        e.firstDueDate()
                )).toList();
        return ResponseEntity.ok(ApiResponse.ok(
                checkDuplicateExpensesUseCase.execute(new CardId(cardId), commands)));
    }
}
