package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.model.account.AccountType;
import com.financialapp.banks.web.dto.request.AccountRequest;
import com.financialapp.banks.web.dto.request.BankRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void seedBank() throws Exception {
        mockMvc.perform(post("/api/v1/banks")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BankRequest("GALICIA", null))));
    }

    @Test
    void createAccount_then_getByCbu() throws Exception {
        AccountRequest req = new AccountRequest(
                "GALICIA", "Savings", AccountType.SAVINGS.name(),
                new BigDecimal("1000.00"), "USD", true,
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
                .andExpect(jsonPath("$.data.balance").value(1000.00));
    }
}
