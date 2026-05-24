package com.financialapp.banks.web.controller;

import com.financialapp.banks.application.account.command.*;
import com.financialapp.banks.application.account.usecase.*;
import com.financialapp.banks.domain.exception.account.InvalidDateRangeException;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.request.AccountRequest;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.AccountTransactionResponse;
import com.financialapp.banks.web.dto.response.ApiResponse;
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
    private final CreateAccountUseCase createAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final AdjustBalanceUseCase adjustBalanceUseCase;
    private final GetAccountTransactionsUseCase getTransactionsUseCase;
    private final AccountWebMapper accountMapper;

    @GetMapping
    @Operation(summary = "List all accounts for the current user")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "false") boolean hideEmpty) {
        Currency cur = currency != null ? Currency.getInstance(currency) : null;
        BankName bank = bankName != null ? BankName.valueOf(bankName) : null;
        List<AccountResponse> result = listAccountsUseCase.execute(
                new FilterAccountCommand(new UserId(userId), type, cur, bank, name, hideEmpty))
                .stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{cbu}")
    @Operation(summary = "Get an account by CBU")
    public ResponseEntity<ApiResponse<AccountResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu) {
        return ResponseEntity.ok(ApiResponse.ok(
                accountMapper.toResponse(getAccountUseCase.execute(cbu))));
    }

    @PostMapping
    @Operation(summary = "Create an account inside a bank")
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AccountRequest request) {
        Money initialBalance = new Money(request.balance(), Currency.getInstance(request.currency()));
        var result = createAccountUseCase.execute(new CreateAccountCommand(
                new UserId(userId),
                BankName.valueOf(request.bankName()),
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

    @PutMapping("/{cbu}")
    @Operation(summary = "Update an account")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu,
            @Valid @RequestBody AccountRequest request) {
        Money balance = new Money(request.balance(), Currency.getInstance(request.currency()));
        var result = updateAccountUseCase.execute(new UpdateAccountCommand(
                cbu, request.name(), balance, request.isActive()));
        return ResponseEntity.ok(ApiResponse.ok(accountMapper.toResponse(result)));
    }

    @DeleteMapping("/{cbu}")
    @Operation(summary = "Delete an account")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu,
            @RequestParam String bankName) {
        deleteAccountUseCase.execute(new DeleteAccountCommand(cbu, BankName.valueOf(bankName)));
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }

    @GetMapping("/{cbu}/transactions")
    @Operation(summary = "Get account transactions. Default: last 5. Use ?all=true or ?from=&to= for date filtering.")
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
                .map(t -> new AccountTransactionResponse(
                        t.transactionId(), t.accountCbu(),
                        t.amount().amount(), t.amount().currency().getCurrencyCode(),
                        t.description(), t.category(), t.subcategory(), t.date()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{cbu}/balance/adjust")
    @Operation(summary = "Adjust account balance")
    public ResponseEntity<ApiResponse<Void>> adjustBalance(
            @PathVariable String cbu,
            @RequestParam BigDecimal delta,
            @RequestParam String currency) {
        adjustBalanceUseCase.execute(new AdjustBalanceCommand(cbu, new Money(delta, Currency.getInstance(currency))));
        return ResponseEntity.ok(ApiResponse.ok("Balance adjusted", null));
    }
}
