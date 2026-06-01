package com.financialapp.banks.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetadataControllerIT {

    @Autowired MockMvc mockMvc;

    @Test
    void catalog_returnsAllEnumValues() throws Exception {
        mockMvc.perform(get("/api/v1/banks/metadata")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountTypes",
                        org.hamcrest.Matchers.containsInAnyOrder("CHECKING", "SAVINGS", "INVESTMENT")))
                .andExpect(jsonPath("$.data.cardTypes",
                        org.hamcrest.Matchers.hasItems("STANDARD", "GOLD", "BLACK")))
                .andExpect(jsonPath("$.data.cardBrands",
                        org.hamcrest.Matchers.containsInAnyOrder("VISA", "MASTERCARD", "AMEX")))
                .andExpect(jsonPath("$.data.cardBehaviors",
                        org.hamcrest.Matchers.containsInAnyOrder("CREDIT", "INSTANT_PAYMENT")));
    }
}
