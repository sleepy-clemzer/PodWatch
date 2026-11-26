package io.podwatch.system;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.podwatch.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SystemInfoController.class)
@Import(SecurityConfig.class)
class SystemInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsApplicationNameAndVersionWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("podwatch"))
                // Pas de BuildProperties dans une tranche de test MVC → version "dev"
                .andExpect(jsonPath("$.version").value(SystemInfoController.DEV_VERSION));
    }

    @Test
    void exposesOnlyNameAndVersion() throws Exception {
        mockMvc.perform(get("/api/v1/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
