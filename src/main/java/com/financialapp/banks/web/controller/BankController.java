package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.request.BankRequest;
import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.model.dto.response.BankResponse;
import com.financialapp.banks.service.BankService;
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

    private final BankService bankService;

    @GetMapping
    @Operation(summary = "List user banks with their accounts")
    public ResponseEntity<ApiResponse<List<BankResponse>>> list(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(bankService.list(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single bank with its accounts")
    public ResponseEntity<ApiResponse<BankResponse>> get(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(bankService.get(id, userId)));
    }

    @PostMapping
    @Operation(summary = "Create a bank")
    public ResponseEntity<ApiResponse<BankResponse>> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BankRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bank created", bankService.create(userId, request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a bank")
    public ResponseEntity<ApiResponse<BankResponse>> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody BankRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(bankService.update(id, userId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a bank and cascade its accounts")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        bankService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Bank deleted", null));
    }
}
