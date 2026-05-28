package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.usecase.account.GetAccountTransactionsUseCase;
import com.financialapp.banks.domain.common.model.Money;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.domain.port.FinancesPort.TransactionSummary;
import com.financialapp.banks.domain.exception.FinancesServiceException;
import com.financialapp.banks.web.dto.request.AccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean GetAccountTransactionsUseCase getTransactionsUseCase;

    @Test
    void createAccount_then_getByCbu() throws Exception {
        AccountRequest req = new AccountRequest(
                "GALICIA", "Savings", AccountType.SAVINGS,
                "USD", true,
                "1234567890123456789012", "alias1");

        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.cbu").value("1234567890123456789012"))
                .andExpect(jsonPath("$.data.type").value("SAVINGS"));

        mockMvc.perform(get("/api/v1/banks/accounts/{cbu}", "1234567890123456789012")
                        .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(0));
    }

    @Test
    void getTransactions_defaultReturnsLast5() throws Exception {
        var tx = new TransactionSummary(1L, "CBU1",
                new Money(new BigDecimal("500.00"), Currency.getInstance("ARS")),
                "Supermercado", "Food", "Groceries", LocalDate.of(2026, 5, 1));
        when(getTransactionsUseCase.getRecent(eq("CBU1"), eq(5))).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/v1/banks/accounts/CBU1/transactions")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(500.00))
                .andExpect(jsonPath("$.data[0].currency").value("ARS"));
    }

    @Test
    void getTransactions_allFlagReturnsAll() throws Exception {
        when(getTransactionsUseCase.getAll(eq("CBU1"))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/banks/accounts/CBU1/transactions?all=true")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getTransactions_dateRangeFilter() throws Exception {
        when(getTransactionsUseCase.getFiltered(eq("CBU1"), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/banks/accounts/CBU1/transactions?from=2026-01-01&to=2026-12-31")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactions_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/banks/accounts/CBU1/transactions?from=2026-12-31&to=2026-01-01")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_notFound_returns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/accounts/{cbu}", "0000000000000000000000")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("resource_not_found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getTransactions_invalidDateRange_returns400WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/accounts/{cbu}/transactions", "CBU1")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token")
                .param("from", "2024-12-01")
                .param("to", "2024-01-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("invalid_date_range"));
    }

    @Test
    void getTransactions_serviceUnavailable_returns500WithCode() throws Exception {
        when(getTransactionsUseCase.getRecent(eq("CBU1"), eq(5)))
                .thenThrow(new FinancesServiceException("fetchTransactions", "connection refused"));

        mockMvc.perform(get("/api/v1/banks/accounts/CBU1/transactions")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("finances_service_unavailable"));
    }

    @Test
    void createAccount_malformedCurrency_returns400WithValidationCode() throws Exception {
        AccountRequest req = new AccountRequest(
                "GALICIA", "Savings", AccountType.SAVINGS,
                "US", true,
                "1234567890123456789012", "alias1");

        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));
    }

    @Test
    void createAccount_unknownCurrencyCode_returns400WithCurrencyCode() throws Exception {
        AccountRequest req = new AccountRequest(
                "GALICIA", "Savings", AccountType.SAVINGS,
                "ZZZ", true,
                "1234567890123456789012", "alias1");

        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_currency"));
    }

    @Test
    void createAccount_invalidType_returns400() throws Exception {
        String body = """
                {"bankName":"GALICIA","name":"Savings","type":"FOO","balance":1000.00,
                 "currency":"USD","isActive":true,"cbu":"1234567890123456789012","alias":"alias1"}
                """;

        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_request"));
    }

    @Test
    void listAccounts_noCurrencyFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/banks/accounts?hideEmpty=false")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void listAccounts_lowercaseCurrencyFilter_normalizedReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/banks/accounts?currency=usd")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createAccount_lowercaseCurrency_normalizedToUppercase() throws Exception {
        AccountRequest req = new AccountRequest(
                "GALICIA", "Savings", AccountType.SAVINGS,
                "usd", true,
                "1234567890123456789012", "alias1");

        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }
}
