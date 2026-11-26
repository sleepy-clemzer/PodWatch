package io.podwatch.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.podwatch.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Utilise le faux {@code index.html} de {@code src/test/resources/static/}.
 */
@WebMvcTest
@Import(SecurityConfig.class)
class SpaWebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/dashboard", "/pods", "/pods/prod-eu/default/my-pod"})
    void servesIndexHtmlForSpaRoutes(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("podwatch-test-index")));
    }

    /** Sur « / », c'est la page d'accueil de Spring Boot qui fait un forward (non suivi par MockMvc). */
    @Test
    void rootForwardsToIndexHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void returns404ForMissingAsset() throws Exception {
        mockMvc.perform(get("/assets/missing.js")).andExpect(status().isNotFound());
    }

    @Test
    void classifiesPaths() {
        assertThat(SpaWebConfig.isSpaRoute("pods/abc")).isTrue();
        assertThat(SpaWebConfig.isSpaRoute("")).isTrue();
        assertThat(SpaWebConfig.isSpaRoute("assets/index-abc.js")).isFalse();
        assertThat(SpaWebConfig.isSpaRoute("api/v1/unknown")).isFalse();
        assertThat(SpaWebConfig.isSpaRoute("api")).isFalse();
        assertThat(SpaWebConfig.isSpaRoute("actuator/health")).isFalse();
        assertThat(SpaWebConfig.isSpaRoute("ws/logs")).isFalse();
    }
}
