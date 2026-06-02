package com.financialapp.banks.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers the InternalAuthFilter empty-configured-token branch via a property override. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "internal.auth.token=")
class InternalAuthEmptyTokenIT {

    @Autowired MockMvc mockMvc;

    @Test
    void rejectsAllRequests_whenConfiguredTokenEmpty() throws Exception {
        // Given the service is configured with an empty internal token (the isEmpty() branch)
        // When any protected request arrives / Then it is rejected
        mockMvc.perform(get("/api/v1/banks/available")
                        .header("X-User-Id", "1").header("X-Internal-Token", "anything"))
                .andExpect(status().isUnauthorized());
    }
}
