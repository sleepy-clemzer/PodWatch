package io.podwatch.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Garantit la règle de la Phase 1 : rien n'est accessible sur l'API en dehors des endpoints publics.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/clusters",
            "/api/v1/clusters/prod/namespaces/default/pods",
            "/ws/logs",
            "/actuator/env",
            "/actuator/beans"
    })
    void deniesEverythingOutsidePublicEndpoints(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isForbidden());
    }

    @Test
    void deniesWriteRequestsOnApi() throws Exception {
        mockMvc.perform(delete("/api/v1/clusters/prod/namespaces/default/pods/x"))
                .andExpect(status().isForbidden());
    }

    @Test
    void setsSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/system/info"))
                .andExpect(header().string("Content-Security-Policy", SecurityConfig.CONTENT_SECURITY_POLICY))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
