package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.web.dto.request.BankRequest;
import com.financialapp.banks.web.dto.request.CardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CardControllerIT {

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
    void createCard_then_listIncludesIt() throws Exception {
        CardRequest req = new CardRequest("GALICIA", CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.CREDIT, "1234", LocalDate.now().plusYears(2), 20, 10);

        mockMvc.perform(post("/api/v1/banks/cards")
                        .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.last4Digits").value("1234"));

        mockMvc.perform(get("/api/v1/banks/cards").header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].last4Digits").value("1234"));
    }
}
