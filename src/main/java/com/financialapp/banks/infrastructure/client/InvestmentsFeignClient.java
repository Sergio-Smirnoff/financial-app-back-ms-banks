package com.financialapp.banks.client;

import com.financialapp.banks.model.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "ms-investments", url = "${investments.service.url:http://localhost:8086}")
public interface InvestmentsClient {

    @GetMapping("/api/v1/investments/holdings/valuation")
    ApiResponse<AccountValuation> getValuation(@RequestParam("accountId") Long accountId);

    @GetMapping("/api/v1/investments/holdings/count")
    ApiResponse<Long> countHoldings(@RequestParam("accountId") Long accountId);

    record AccountValuation(Long accountId, BigDecimal totalValuation, String currency) {}
}
