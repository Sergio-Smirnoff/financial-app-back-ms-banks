package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BankControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void getBank_seeded_returnsBank() throws Exception {
        mockMvc.perform(get("/api/v1/banks/{bankNumber}", "007")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankNumber").value("007"))
                .andExpect(jsonPath("$.data.name").value("GALICIA"));
    }

    @Test
    void getBank_invalidNumber_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/banks/{bankNumber}", "NOT_A_BANK")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_bank_number"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void availableBanks_returnsTheSeededCatalog() throws Exception {
        mockMvc.perform(get("/api/v1/banks/available")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(13))
                .andExpect(jsonPath("$.data[*].bankNumber", hasItem("007")))
                .andExpect(jsonPath("$.data[*].name", hasItem("GALICIA")));
    }
}
