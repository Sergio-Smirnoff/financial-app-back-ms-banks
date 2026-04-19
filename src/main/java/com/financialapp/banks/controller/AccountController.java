package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.request.AccountRequest;
import com.financialapp.banks.model.dto.response.AccountResponse;
import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank accounts management")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "List all accounts for the current user (across banks)")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> list(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.listByUser(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by id")
    public ResponseEntity<ApiResponse<AccountResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.get(id, userId)));
    }

    @PatchMapping("/{id}/balance/adjust")
    @Operation(summary = "Adjust account balance (internal use)")
    public ResponseEntity<ApiResponse<Void>> adjustBalance(
            @PathVariable Long id,
            @RequestParam java.math.BigDecimal delta) {
        accountService.adjustBalance(id, delta);
        return ResponseEntity.ok(ApiResponse.ok("Balance adjusted", null));
    }

    @PostMapping
    @Operation(summary = "Create an account inside a bank")
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", accountService.create(userId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an account")
    public ResponseEntity<ApiResponse<AccountResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        accountService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Account deleted", null));
    }
}
