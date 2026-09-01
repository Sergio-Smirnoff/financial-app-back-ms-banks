package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.domain.exception.DomainError;
import com.financialapp.banks.domain.usecase.fee.GetUserFees;
import com.financialapp.banks.domain.usecase.fee.UpsertAccountFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.UpsertCardFeeSchedule;
import com.financialapp.banks.domain.usecase.fee.command.UpsertAccountFeeScheduleCommand;
import com.financialapp.banks.domain.usecase.fee.command.UpsertCardFeeScheduleCommand;
import com.financialapp.banks.web.dto.request.AccountFeeScheduleRequest;
import com.financialapp.banks.web.dto.request.CardFeeScheduleRequest;
import com.financialapp.banks.web.dto.response.AccountFeeScheduleResponse;
import com.financialapp.banks.web.dto.response.CardFeeScheduleResponse;
import com.financialapp.banks.web.dto.response.UserFeesResponse;
import com.financialapp.banks.web.mapper.FeeWebMapper;
import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.openapi.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
@Validated
@Tag(name = "Fees", description = "Account and card fee schedule management")
public class FeeController {

    private final UpsertAccountFeeSchedule upsertAccountFeeSchedule;
    private final UpsertCardFeeSchedule upsertCardFeeSchedule;
    private final GetUserFees getUserFees;
    private final FeeWebMapper feeWebMapper;

    @PutMapping("/accounts/{cbu}/fees")
    @Operation(summary = "Upsert fee schedule for an account")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "invalid_fee_schedule"})
    public ResponseEntity<ApiResponse<AccountFeeScheduleResponse>> upsertAccountFees(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cbu,
            @Valid @RequestBody AccountFeeScheduleRequest request) {
        var schedule = upsertAccountFeeSchedule.execute(new UpsertAccountFeeScheduleCommand(
                new UserId(userId),
                cbu,
                request.maintenanceFee(),
                request.transferFee(),
                request.currency(),
                request.ivaTreatment()
        ));
        return ResponseEntity.ok(ApiResponse.ok("Account fee schedule updated", feeWebMapper.toResponse(schedule)));
    }

    @PutMapping("/cards/{cardNumber}/fees")
    @Operation(summary = "Upsert fee schedule for a card")
    @ApiErrorCodes(catalog = DomainError.class, value = {"resource_not_found", "invalid_fee_schedule"})
    public ResponseEntity<ApiResponse<CardFeeScheduleResponse>> upsertCardFees(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable String cardNumber,
            @Valid @RequestBody CardFeeScheduleRequest request) {
        var schedule = upsertCardFeeSchedule.execute(new UpsertCardFeeScheduleCommand(
                new UserId(userId),
                cardNumber,
                request.annualFee(),
                request.internationalSurchargePct(),
                request.currency(),
                request.ivaTreatment()
        ));
        return ResponseEntity.ok(ApiResponse.ok("Card fee schedule updated", feeWebMapper.toResponse(schedule)));
    }

    @GetMapping("/fees")
    @Operation(summary = "Get all account and card fee schedules for current user")
    public ResponseEntity<ApiResponse<UserFeesResponse>> getUserFees(
            @RequestHeader("X-User-Id") Long userId) {
        var result = getUserFees.execute(new UserId(userId));
        return ResponseEntity.ok(ApiResponse.ok(feeWebMapper.toResponse(result)));
    }
}
