package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.bank.ListAvailableBanksUseCase;
import com.financialapp.banks.domain.usecase.bank.ListBanksUseCase;
import com.financialapp.banks.domain.usecase.bank.GetBankUseCase;
import com.financialapp.banks.domain.usecase.account.command.FilterAccountCommand;
import com.financialapp.banks.domain.usecase.account.ListAccountsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.model.bank.Bank;
import com.financialapp.banks.domain.model.bank.BankNumber;
import com.financialapp.banks.web.dto.response.AccountResponse;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import com.financialapp.banks.web.dto.response.AvailableBankResponse;
import com.financialapp.banks.web.dto.response.BankResponse;
import com.financialapp.banks.web.mapper.AccountWebMapper;
import com.financialapp.banks.web.mapper.BankWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
@Tag(name = "Banks", description = "Bank catalog (read-only) and the user's banks")
public class BankController {

    private final ListBanksUseCase listBanksUseCase;
    private final GetBankUseCase getBankUseCase;
    private final ListAccountsUseCase listAccountsUseCase;
    private final ListAvailableBanksUseCase listAvailableBanksUseCase;
    private final AccountWebMapper accountMapper;
    private final BankWebMapper bankMapper;

    @GetMapping
    @Operation(summary = "List the user's banks (those where they hold accounts)")
    public ResponseEntity<ApiResponse<List<BankResponse>>> list(@RequestHeader("X-User-Id") Long userId) {
        List<BankResponse> responses = listBanksUseCase.execute(new UserId(userId)).stream()
                .map(bankWithAccounts -> bankMapper.toResponse(
                        bankWithAccounts.bank(),
                        bankWithAccounts.accounts().stream().map(accountMapper::toResponse).toList()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/available")
    @Operation(summary = "List all available bank names (catalog)")
    public ResponseEntity<ApiResponse<List<AvailableBankResponse>>> available() {
        return ResponseEntity.ok(ApiResponse.ok(
                listAvailableBanksUseCase.execute().stream()
                        .map(bankMapper::toAvailableBank)
                        .toList()));
    }

    @GetMapping("/{bankNumber}")
    @Operation(summary = "Get a bank with the user's accounts there")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found"})
    public ResponseEntity<ApiResponse<BankResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String bankNumber) {
        Bank bank = getBankUseCase.execute(new BankNumber(bankNumber));
        List<AccountResponse> accounts = listAccountsUseCase.execute(
                new FilterAccountCommand(new UserId(userId), null, null, bank.bankNumber(), null, false))
                .stream().map(accountMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(bankMapper.toResponse(bank, accounts)));
    }
}
