package io.podwatch.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité — <strong>Phase 1</strong>.
 *
 * <p>Tant que l'authentification n'existe pas (Phase 2, PW-301), la règle est simple :
 * <ul>
 *   <li>publics : la SPA (fichiers statiques), {@code /actuator/health}, {@code /api/v1/system/info} ;</li>
 *   <li><strong>tout le reste de l'API, des WebSockets et d'Actuator est refusé</strong> (403).</li>
 * </ul>
 * Aucune donnée de cluster ne peut donc être exposée par erreur avant la mise en place de l'authentification.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** CSP stricte. {@code style-src 'unsafe-inline'} est requis par Ant Design (styles CSS-in-JS injectés). */
    static final String CONTENT_SECURITY_POLICY = String.join("; ",
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline'",
            "img-src 'self' data:",
            "font-src 'self' data:",
            "connect-src 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'",
            "form-action 'self'");

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/system/info").permitAll()
                        .requestMatchers("/api/**", "/ws/**", "/actuator/**").denyAll()
                        // Le reste = la SPA React (index.html, assets) : public, elle ne contient aucune donnée.
                        .anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CONTENT_SECURITY_POLICY))
                        .frameOptions(frame -> frame.deny()));
        return http.build();
    }

    /**
     * Aucun utilisateur en Phase 1. Ce bean empêche Spring Boot de générer un utilisateur
     * par défaut avec un mot de passe affiché dans les logs. Remplacé en Phase 2.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
