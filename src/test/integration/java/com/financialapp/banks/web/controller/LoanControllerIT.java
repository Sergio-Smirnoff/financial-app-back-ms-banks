package com.financialapp.banks.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LoanControllerIT {

    @Autowired MockMvc mockMvc;

    @Test
    void getLoanInstallments_notFound_returns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/loans/{id}/installments", 99999L)
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("resource_not_found"))
            .andExpect(jsonPath("$.status").value(404));
    }
}
