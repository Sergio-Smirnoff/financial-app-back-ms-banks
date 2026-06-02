package com.financialapp.banks.web.dto.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void ok_withData_setsOkStatusAndTitle() {
        // Given / When
        ApiResponse<String> response = ApiResponse.ok("payload");

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getTitle()).isEqualTo("OK");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void ok_withMessageAndData_carriesMessage() {
        // Given / When
        ApiResponse<String> response = ApiResponse.ok("done", "payload");

        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("done");
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void created_withData_setsCreatedStatusAndTitle() {
        // Given / When (the single-arg created variant)
        ApiResponse<String> response = ApiResponse.created("payload");

        // Then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getTitle()).isEqualTo("Created");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void created_withMessageAndData_carriesMessage() {
        // Given / When
        ApiResponse<String> response = ApiResponse.created("made", "payload");

        // Then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getMessage()).isEqualTo("made");
        assertThat(response.getData()).isEqualTo("payload");
    }
}
