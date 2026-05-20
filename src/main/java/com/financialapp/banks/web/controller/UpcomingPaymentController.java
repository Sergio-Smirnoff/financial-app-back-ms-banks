package com.financialapp.banks.controller;

import com.financialapp.banks.model.dto.response.ApiResponse;
import com.financialapp.banks.model.dto.response.UpcomingPaymentResponse;
import com.financialapp.banks.service.UpcomingPaymentService;
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

    private final UpcomingPaymentService upcomingPaymentService;

    @GetMapping
    @Operation(summary = "Get upcoming loan and card installments")
    public ResponseEntity<ApiResponse<List<UpcomingPaymentResponse>>> getUpcomingPayments(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.ok(upcomingPaymentService.getUpcomingPayments(userId, from, to)));
    }
}
