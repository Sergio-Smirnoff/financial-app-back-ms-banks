package com.financialapp.banks.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Anti-corruption envelope for responses from other services (ms-finances, ms-investments).
 * Those services wrap payloads in the shared {@code ApiResponse} shape, but this layer must
 * not depend on our web layer's DTO. Only the {@code data} payload matters here; every other
 * envelope field (status, title, message, errors, timestamp) is ignored on deserialization.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalApiResponse<T>(T data) {
}
