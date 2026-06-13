package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.web.dto.request.AccountRequest;
import com.financialapp.banks.web.dto.request.LoanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void getLoanInstallments_notFound_returns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/loans/{id}/installments", 99999L)
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("resource_not_found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listLoans_filteredByBank_excludesOtherUsersLoans() throws Exception {
        // Bank "007" (GALICIA) is seeded by BankCatalogSeeder at startup.
        // Open a checking account for user 1 on bank 007 so the loan origination has a valid destination CBU.
        AccountRequest accountReq = new AccountRequest(
                "007", "Checking", AccountType.CHECKING,
                "ARS", true,
                "0070001600000000123459", "alias-loan-test");
        mockMvc.perform(post("/api/v1/banks/accounts")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountReq)))
                .andExpect(status().isCreated());

        // Originate a loan for user 1 on bank 007.
        LoanRequest loanReq = new LoanRequest(
                "007",
                "0070001600000000123459",
                "Test Loan",
                "10000.00",
                "5.0",
                12,
                LocalDate.of(2026, 1, 1));
        mockMvc.perform(post("/api/v1/banks/loans")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanReq)))
                .andExpect(status().isCreated());

        // User 999 requests loans filtered by bank 007 — must get an empty list (not user 1's loan).
        mockMvc.perform(get("/api/v1/banks/loans")
                        .param("bankNumber", "007")
                        .header("X-User-Id", "999")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
