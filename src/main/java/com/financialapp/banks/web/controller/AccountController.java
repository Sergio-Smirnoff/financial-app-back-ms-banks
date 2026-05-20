package com.financialapp.banks.web.controller;

import com.financialapp.banks.application.account.command.*;
import com.financialapp.banks.application.account.usecase.*;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.account.AccountId;
import com.financialapp.banks.domain.model.account.AccountInformation;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.web.dto.request.AccountRequest;
import com.financialapp.banks.web.dto.response.AccountResponse;
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
    private final AccountWebMapper accountMapper;

    @GetMapping
    @Operation(summary = "List all accounts for the current user")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) String currency) {
        Currency cur = currency != null ? Currency.getInstance(currency) : null;
        List<AccountResponse> result = listAccountsUseCase.execute(
                new FilterAccountCommand(new UserId(userId), type, cur, null))
                .stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by id")
    public ResponseEntity<ApiResponse<AccountResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(
                accountMapper.toResponse(getAccountUseCase.execute(new AccountId(id)))));
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

    @PutMapping("/{id}")
    @Operation(summary = "Update an account")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {
        Money balance = new Money(request.balance(), Currency.getInstance(request.currency()));
        var result = updateAccountUseCase.execute(new UpdateAccountCommand(
                new AccountId(id), request.name(), balance, request.isActive()));
        return ResponseEntity.ok(ApiResponse.ok(accountMapper.toResponse(result)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        deleteAccountUseCase.execute(new DeleteAccountCommand(new AccountId(id), null));
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }

    @PostMapping("/{id}/balance/adjust")
    @Operation(summary = "Adjust account balance")
    public ResponseEntity<ApiResponse<Void>> adjustBalance(
            @PathVariable Long id,
            @RequestParam BigDecimal delta,
            @RequestParam(required = false) String currency) {
        Currency cur = currency != null ? Currency.getInstance(currency) : null;
        adjustBalanceUseCase.execute(new AdjustBalanceCommand(new AccountId(id), new Money(delta, cur)));
        return ResponseEntity.ok(ApiResponse.ok("Balance adjusted", null));
    }
}
