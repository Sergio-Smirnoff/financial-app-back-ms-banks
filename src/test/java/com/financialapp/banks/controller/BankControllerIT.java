package com.financialapp.banks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.model.dto.request.BankRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class BankControllerIT {

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper objectMapper;

    @Test
    void createBank_then_listIncludesIt() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        BankRequest req = new BankRequest("Chase", null);
        mockMvc.perform(post("/api/v1/banks")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Chase"));

        mockMvc.perform(get("/api/v1/banks").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Chase"));
    }
}
