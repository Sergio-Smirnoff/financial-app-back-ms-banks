package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.account.command.*;
import com.financialapp.banks.domain.usecase.account.*;
import com.financialapp.banks.domain.exception.account.InvalidDateRangeException;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.web.dto.request.AccountRequest;
import com.financialapp.banks.web.dto.request.UpdateAccountRequest;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.AccountTransactionResponse;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.banks.web.mapper.AccountWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank accounts management")
public class AccountController {

    private final ListAccountsUseCase listAccountsUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final OpenAccountUseCase openAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final CloseAccountUseCase closeAccountUseCase;
    private final AdjustBalanceUseCase adjustBalanceUseCase;
    private final GetAccountTransactionsUseCase getTransactionsUseCase;
    private final AccountWebMapper accountMapper;

    @GetMapping
    @Operation(summary = "List all accounts for the current user")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String bankNumber,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "false") boolean hideEmpty) {
        Currency cur = currency != null ? Money.parseCurrency(currency) : null;
        BankNumber bank = bankNumber != null ? new BankNumber(bankNumber) : null;
        List<AccountResponse> result = listAccountsUseCase.execute(
                new FilterAccountCommand(new UserId(userId), type, cur, bank, name, hideEmpty))
                .stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{cbu}")
    @Operation(summary = "Get an account by CBU")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "invalid_cbu"})
    public ResponseEntity<ApiResponse<AccountResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu) {
        return ResponseEntity.ok(ApiResponse.ok(
                accountMapper.toResponse(getAccountUseCase.execute(cbu))));
    }

    @PostMapping
    @Operation(summary = "Create an account inside a bank")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_already_exists", "invalid_cbu", "cbu_bank_mismatch", "invalid_account_number", "invalid_currency"})
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AccountRequest request) {
        Money initialBalance = Money.of(BigDecimal.ZERO, request.currency());
        var result = openAccountUseCase.execute(new OpenAccountCommand(
                new UserId(userId),
                new BankNumber(request.bankNumber()),
                request.name(),
                request.type(),
                initialBalance,
                request.isActive() != null ? request.isActive() : true,
                request.cbu(),
                request.alias()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", accountMapper.toResponse(result)));
    }

    @PatchMapping("/{cbu}")
    @Operation(summary = "Update an account")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "resource_already_exists", "account_invalid_type"})
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu,
            @Valid @RequestBody UpdateAccountRequest request) {
        Money balance = toMoney(request.balance(), request.currency());
        var result = updateAccountUseCase.execute(new UpdateAccountCommand(
                cbu, request.name(), balance, request.isActive()));
        return ResponseEntity.ok(ApiResponse.ok(accountMapper.toResponse(result)));
    }

    @DeleteMapping("/{cbu}")
    @Operation(summary = "Delete an account")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "account_not_deletable", "investments_service_unavailable"})
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu) {
        closeAccountUseCase.execute(new CloseAccountCommand(cbu));
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }

    @GetMapping("/{cbu}/transactions")
    @Operation(summary = "Get account transactions. Default: last 5. Use ?all=true or ?from=&to= for date filtering.")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "invalid_date_range", "finances_service_unavailable"})
    public ResponseEntity<ApiResponse<List<AccountTransactionResponse>>> getTransactions(
            @PathVariable String cbu,
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDateRangeException(from.toString(), to.toString());
        }

        var transactions = from != null && to != null
                ? getTransactionsUseCase.getFiltered(cbu, from, to)
                : all ? getTransactionsUseCase.getAll(cbu) : getTransactionsUseCase.getRecent(cbu, 5);

        List<AccountTransactionResponse> response = transactions.stream()
                .map(transaction -> new AccountTransactionResponse(
                        transaction.transactionId(), transaction.accountCbu(),
                        transaction.amount().amount().toPlainString(), transaction.amount().currency().getCurrencyCode(),
                        transaction.description(), transaction.category(), transaction.subcategory(), transaction.date()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{cbu}/balance/adjust")
    @Operation(summary = "Adjust account balance")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "account_insufficient_funds", "account_currency_mismatch", "account_investment_restriction"})
    public ResponseEntity<ApiResponse<Void>> adjustBalance(
            @PathVariable String cbu,
            @RequestParam String delta,
            @RequestParam String currency) {
        adjustBalanceUseCase.execute(new AdjustBalanceCommand(cbu, Money.of(new BigDecimal(delta), currency)));
        return ResponseEntity.ok(ApiResponse.ok("Balance adjusted", null));
    }

    private Money toMoney(String amount, String currency) {
        return amount != null && currency != null
                ? Money.of(new BigDecimal(amount), currency)
                : null;
    }
}
