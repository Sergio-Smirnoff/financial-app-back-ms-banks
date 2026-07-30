package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.usecase.snapshot.GetBalanceSnapshots;
import com.financialapp.banks.domain.usecase.snapshot.command.GetBalanceSnapshotsCommand;
import com.financialapp.banks.web.dto.response.BalanceSnapshotResponse;
import com.financialapp.banks.web.mapper.BalanceSnapshotWebMapper;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/balance-snapshots")
@RequiredArgsConstructor
@Tag(name = "Balance Snapshots", description = "Daily balance snapshot history for net worth calculations")
public class BalanceSnapshotController {

    private final GetBalanceSnapshots getBalanceSnapshots;
    private final BalanceSnapshotWebMapper webMapper;

    @GetMapping
    @Operation(summary = "Get daily balance snapshot history for a date range")
    @ApiErrorCodes(catalog = DomainError.class, value = {"invalid_date_range"})
    public ResponseEntity<ApiResponse<List<BalanceSnapshotResponse>>> getSnapshots(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var snapshots = getBalanceSnapshots.execute(new GetBalanceSnapshotsCommand(new UserId(userId), from, to));
        var response = snapshots.stream().map(webMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
