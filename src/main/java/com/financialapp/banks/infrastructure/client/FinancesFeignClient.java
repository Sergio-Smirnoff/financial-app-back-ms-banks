package com.financialapp.banks.infrastructure.client;

import com.financialapp.banks.infrastructure.client.dto.ExternalApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "ms-finances", url = "${finances.service.url:http://localhost:8082}")
public interface FinancesFeignClient {

    record TransactionDto(
            Long transactionId,
            String accountCbu,
            BigDecimal amount,
            String currency,
            String description,
            String category,
            String subcategory,
            LocalDate date
    ) {}

    @GetMapping("/api/v1/finances/transactions")
    ExternalApiResponse<List<TransactionDto>> getTransactions(
            @RequestParam("accountCbu") String accountCbu,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "from", required = false) LocalDate from,
            @RequestParam(value = "to", required = false) LocalDate to
    );
}
