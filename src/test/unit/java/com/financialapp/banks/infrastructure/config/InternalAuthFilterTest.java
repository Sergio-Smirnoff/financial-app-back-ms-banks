package com.financialapp.banks.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalAuthFilterTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;
    InternalAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new InternalAuthFilter();
    }

    @Test
    void bypassesActuatorPaths() throws Exception {
        // Given an actuator request (an exempt prefix)
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When the filter runs
        filter.doFilterInternal(request, response, chain);

        // Then the chain proceeds without an auth check
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void bypassesSwaggerUiPaths() throws Exception {
        // Given a swagger-ui request (an exempt prefix)
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // When the filter runs / Then the chain proceeds
        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void bypassesApiDocsPaths() throws Exception {
        // Given an api-docs request (an exempt prefix)
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");

        // When the filter runs / Then the chain proceeds
        filter.doFilterInternal(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsRequestWhenNoConfiguredToken() throws Exception {
        // Given a protected path and no configured internal token (null)
        when(request.getRequestURI()).thenReturn("/api/v1/banks/cards");
        when(request.getHeader("X-Internal-Token")).thenReturn("whatever");
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        // When the filter runs
        filter.doFilterInternal(request, response, chain);

        // Then the request is rejected and the chain is not invoked
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }
}
