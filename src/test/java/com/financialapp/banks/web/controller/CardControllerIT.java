package com.financialapp.banks.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.banks.domain.model.card.CardBehavior;
import com.financialapp.banks.domain.model.card.CardBrand;
import com.financialapp.banks.domain.model.card.CardType;
import com.financialapp.banks.web.dto.request.CardRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CardControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void getCard_notFound_returns404WithCode() throws Exception {
        mockMvc.perform(get("/api/v1/banks/cards/{cardNumber}", "9999999999999999")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("resource_not_found"))
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createCard_then_listIncludesIt() throws Exception {
        CardRequest req = new CardRequest("GALICIA", CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.CREDIT, "1234567890123456", YearMonth.now().plusYears(2), 20, 10);

        mockMvc.perform(post("/api/v1/banks/cards")
                        .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.cardNumber").value("1234567890123456"));

        mockMvc.perform(get("/api/v1/banks/cards").header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cardNumber").value("1234567890123456"));
    }

    @Test
    void createCard_acceptsMmYyExpiryString() throws Exception {
        String body = """
                {"bankName":"GALICIA","brand":"VISA","cardType":"STANDARD","behavior":"CREDIT",
                 "cardNumber":"4387269571327193","expiringDate":"08/30","closingDay":15,"dueDay":5}
                """;

        mockMvc.perform(post("/api/v1/banks/cards")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.expiringDate").value("08/30"));
    }

    @Test
    void createCard_rejectsNon16DigitNumber() throws Exception {
        CardRequest req = new CardRequest("GALICIA", CardBrand.VISA, CardType.PLATINUM,
                CardBehavior.CREDIT, "1234", YearMonth.now().plusYears(2), 20, 10);

        mockMvc.perform(post("/api/v1/banks/cards")
                        .header("X-User-Id", "1")
                        .header("X-Internal-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"));
    }

    @Test
    void getCard_rejectsNon16DigitPathVar() throws Exception {
        mockMvc.perform(get("/api/v1/banks/cards/{cardNumber}", "12ab")
                .header("X-User-Id", "1")
                .header("X-Internal-Token", "test-token"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("validation_error"));
    }
}
