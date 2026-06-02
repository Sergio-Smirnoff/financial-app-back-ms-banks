package com.financialapp.banks.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers the InternalAuthFilter mismatch branch with a configured (non-empty) token. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternalAuthFilterIT {

    @Autowired MockMvc mockMvc;

    @Test
    void rejectsRequestWithWrongInternalToken() throws Exception {
        // Given a protected endpoint / When the X-Internal-Token does not match the configured token
        mockMvc.perform(get("/api/v1/banks/available")
                        .header("X-User-Id", "1").header("X-Internal-Token", "wrong-token"))
                // Then the request is rejected
                .andExpect(status().isUnauthorized());
    }
}
