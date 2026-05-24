package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.web.dto.request.BankRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    void createBank_then_listIncludesIt() throws Exception {
        BankRequest req = new BankRequest("GALICIA", null);

        mockMvc.perform(post("/api/v1/banks")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("GALICIA"));

        mockMvc.perform(get("/api/v1/banks/{name}", "GALICIA")
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("GALICIA"));
    }

    @Test
    void getBank_notFound_returns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/{name}", "SANTANDER")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("resource_not_found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void availableBanks_returnsAllEnumValues() throws Exception {
        mockMvc.perform(get("/api/v1/banks/available")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(13))
                .andExpect(jsonPath("$.data[?(@.name == 'GALICIA')].displayName").value("Galicia"))
                .andExpect(jsonPath("$.data[?(@.name == 'SANTANDER')].displayName").value("Santander"));
    }
}
