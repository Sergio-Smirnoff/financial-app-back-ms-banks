package com.financialapp.banks.web.controller;

import com.financialapp.banks.domain.usecase.upcoming.GetUpcomingPaymentsUseCase;
import com.financialapp.banks.domain.common.model.UserId;
import com.financialapp.banks.web.dto.response.ApiResponse;
import com.financialapp.banks.web.dto.response.UpcomingPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/banks/upcoming-payments")
@RequiredArgsConstructor
@Tag(name = "Upcoming Payments", description = "Consolidated view of upcoming installments")
public class UpcomingPaymentController {

    private final GetUpcomingPaymentsUseCase getUpcomingPaymentsUseCase;

    @GetMapping
    @Operation(summary = "Get upcoming loan and card installments")
    public ResponseEntity<ApiResponse<List<UpcomingPaymentResponse>>> getUpcomingPayments(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<UpcomingPaymentResponse> result = getUpcomingPaymentsUseCase
                .execute(new UserId(userId), from, to)
                .stream()
                .map(payment -> new UpcomingPaymentResponse(
                        payment.id(), payment.type(), payment.description(),
                        payment.amount().amount().toPlainString(), payment.amount().currency().getCurrencyCode(),
                        payment.dueDate(), payment.installmentNumber(), payment.totalInstallments(), payment.paid()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
