package com.financialapp.banks.web.controller;

import com.financialapp.banks.application.bank.command.CreateBankCommand;
import com.financialapp.banks.application.bank.command.DeleteBankCommand;
import com.financialapp.banks.application.bank.command.UpdateBankCommand;
import com.financialapp.banks.application.bank.usecase.*;
import com.financialapp.banks.application.account.command.FilterAccountCommand;
import com.financialapp.banks.application.account.usecase.ListAccountsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankName;
import com.financialapp.banks.domain.model.bank.Logo;
import com.financialapp.banks.web.dto.request.BankRequest;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.BankResponse;
import com.financialapp.banks.web.mapper.AccountWebMapper;
import com.financialapp.banks.web.mapper.BankWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
@Tag(name = "Banks", description = "User banks management")
public class BankController {

    private final ListBanksUseCase listBanksUseCase;
    private final GetBankUseCase getBankUseCase;
    private final CreateBankUseCase createBankUseCase;
    private final UpdateBankUseCase updateBankUseCase;
    private final DeleteBankUseCase deleteBankUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final AccountWebMapper accountMapper;
    private final BankWebMapper bankMapper;

    @GetMapping
    @Operation(summary = "List all banks with user accounts")
    public ResponseEntity<ApiResponse<List<BankResponse>>> list(@RequestHeader("X-User-Id") Long userId) {
        UserId uid = new UserId(userId);
        List<BankResponse> responses = listBanksUseCase.execute().stream()
                .map(bank -> {
                    List<AccountResponse> accounts = listAccountsUseCase.execute(
                            new FilterAccountCommand(uid, null, null, bank.name()))
                            .stream().map(accountMapper::toResponse).toList();
                    return bankMapper.toResponse(bank, accounts);
                })
                .filter(b -> !b.accounts().isEmpty())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{name}")
    @Operation(summary = "Get a bank by name")
    public ResponseEntity<ApiResponse<BankResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String name) {
        Bank bank = getBankUseCase.execute(BankName.valueOf(name));
        List<AccountResponse> accounts = listAccountsUseCase.execute(
                new FilterAccountCommand(new UserId(userId), null, null, bank.name()))
                .stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(bankMapper.toResponse(bank, accounts)));
    }

    @PostMapping
    @Operation(summary = "Create a bank")
    public ResponseEntity<ApiResponse<BankResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BankRequest request) {
        Bank bank = createBankUseCase.execute(new CreateBankCommand(
                BankName.valueOf(request.name()),
                request.logoUrl() != null ? new Logo(request.logoUrl()) : null));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bank created", bankMapper.toResponse(bank, List.of())));
    }

    @PutMapping("/{name}")
    @Operation(summary = "Update a bank logo")
    public ResponseEntity<ApiResponse<BankResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String name,
            @Valid @RequestBody BankRequest request) {
        Bank bank = updateBankUseCase.execute(new UpdateBankCommand(
                BankName.valueOf(name),
                request.logoUrl() != null ? new Logo(request.logoUrl()) : null));
        return ResponseEntity.ok(ApiResponse.ok(bankMapper.toResponse(bank, List.of())));
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "Delete a bank")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String name) {
        deleteBankUseCase.execute(new DeleteBankCommand(BankName.valueOf(name)));
        return ResponseEntity.ok(ApiResponse.ok("Bank deleted", null));
    }
}
