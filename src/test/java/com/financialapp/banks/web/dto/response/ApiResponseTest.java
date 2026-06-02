package com.financialapp.banks.web.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    // findAndRegisterModules picks up jackson-datatype-jsr310 (on the Spring Boot classpath)
    // so Instant serializes, mirroring the app's configured ObjectMapper.
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void ok_has_success_true_message_data_and_timestamp() throws Exception {
        String json = mapper.writeValueAsString(ApiResponse.ok("hello"));
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":\"hello\""));
        assertTrue(json.contains("\"timestamp\""));
        // legacy fields must be gone
        assertFalse(json.contains("\"status\""));
        assertFalse(json.contains("\"title\""));
    }

    @Test
    void ok_with_message_sets_message() throws Exception {
        String json = mapper.writeValueAsString(ApiResponse.ok("Created", 7));
        assertTrue(json.contains("\"message\":\"Created\""));
        assertTrue(json.contains("\"success\":true"));
    }

    @Test
    void error_has_success_false_and_errors() throws Exception {
        String json = mapper.writeValueAsString(
                ApiResponse.error("Validation failed", List.of("a is required")));
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("\"message\":\"Validation failed\""));
        assertTrue(json.contains("\"errors\":[\"a is required\"]"));
    }
}
