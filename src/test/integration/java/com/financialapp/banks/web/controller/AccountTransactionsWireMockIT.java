package com.financialapp.banks.web.controller;

import com.financialapp.banks.support.WireMockIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the account-transactions endpoint through the REAL ms-finances Feign client
 * (FeignConfig interceptor + FinancesClientAdapter) against a WireMock stub server,
 * replicating the TP1 technique. The existing AccountControllerIT @MockBeans the port;
 * this one does not, so it covers the actual HTTP boundary.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountTransactionsWireMockIT extends WireMockIntegrationTest {

    @Autowired MockMvc mockMvc;

    private static final String CBU = "0070001600000000123459";

    @Test
    void getTransactions_mapsStubbedFinancesResponse_andForwardsInternalToken() throws Exception {
        // Given the finances service returns the canned body (mappings/finances-transactions.json)
        // When the transactions endpoint is hit
        mockMvc.perform(get("/api/v1/banks/accounts/{cbu}/transactions", CBU)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                // Then the stubbed transaction is mapped back through the real Feign client
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].description").value("Coffee"));

        // And the Feign request carried the internal auth header injected by FeignConfig
        wireMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/finances/transactions"))
                .withHeader("X-Internal-Token", equalTo("test-token")));
    }

    @Test
    void getTransactions_propagates5xx_whenFinancesFails() throws Exception {
        // Given the finances service returns a 500 (programmatic stub, higher priority)
        wireMock.stubFor(get(urlPathEqualTo("/api/v1/finances/transactions"))
                .atPriority(1)
                .willReturn(aResponse().withStatus(500)
                        .withHeader("Content-Type", "application/json").withBody("{\"message\":\"boom\"}")));

        // When the endpoint is hit / Then the adapter's failure surfaces as a 5xx
        mockMvc.perform(get("/api/v1/banks/accounts/{cbu}/transactions", CBU)
                        .header("X-User-Id", "1").header("X-Internal-Token", "test-token"))
                .andExpect(status().is5xxServerError());
    }
}
