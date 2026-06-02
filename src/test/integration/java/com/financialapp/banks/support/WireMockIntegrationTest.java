package com.financialapp.banks.support;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base for integration tests that need the downstream finances/investments HTTP
 * boundary stubbed. Replicates the TP1 WireMock recipe: a dynamic-port server with
 * stub files under classpath {@code wiremock/}, with the Feign client base URLs
 * redirected at it via {@link DynamicPropertySource}.
 */
public abstract class WireMockIntegrationTest {

    @RegisterExtension
    protected static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(options().dynamicPort().usingFilesUnderClasspath("wiremock"))
            .build();

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("finances.service.url", wireMock::baseUrl);
        registry.add("investments.service.url", wireMock::baseUrl);
    }
}
