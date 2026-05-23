package com.financialapp.banks.infrastructure.client;

import com.financialapp.banks.web.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ms-investments", url = "${investments.service.url:http://localhost:8086}")
public interface InvestmentsFeignClient {

    @GetMapping("/api/v1/investments/holdings/valuation")
    ApiResponse<AccountValuation> getValuation(@RequestParam("accountCbu") String accountCbu);

    @GetMapping("/api/v1/investments/holdings/count")
    ApiResponse<Long> countHoldings(@RequestParam("accountCbu") String accountCbu);

    record AccountValuation(String accountCbu, BigDecimal totalValuation, String currency) {}
}
