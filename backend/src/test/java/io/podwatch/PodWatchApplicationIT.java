package io.podwatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Test d'intégration : démarre l'application complète sur un vrai PostgreSQL (Testcontainers, Docker requis).
 * Vérifie le démarrage, les migrations Flyway et la santé exposée à Kubernetes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PodWatchApplicationIT {

    /** Même version majeure que docker-compose.yml. */
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void healthIsUpWithDatabase() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void kubernetesProbesAreExposed() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
    }

    @Test
    void flywayCreatesSchemaAndReferenceRoles() {
        var roles = jdbcTemplate.queryForList("SELECT code FROM role ORDER BY id", String.class);
        assertThat(roles).containsExactly("ADMIN", "DEVOPS", "READONLY");

        var tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);
        assertThat(tables).contains("app_user", "role", "user_role", "audit_event");
    }

    @Test
    void oneRolePerEnvironmentIsEnforced() {
        jdbcTemplate.update("INSERT INTO app_user (username) VALUES ('alice')");
        Long id = jdbcTemplate.queryForObject("SELECT id FROM app_user WHERE username = 'alice'", Long.class);
        jdbcTemplate.update("INSERT INTO user_role (user_id, role_id, environment) VALUES (?, 2, 'PREPROD')", id);
        jdbcTemplate.update("INSERT INTO user_role (user_id, role_id, environment) VALUES (?, 3, 'PROD')", id);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        jdbcTemplate.update("INSERT INTO user_role (user_id, role_id, environment) VALUES (?, 1, 'PROD')", id))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
