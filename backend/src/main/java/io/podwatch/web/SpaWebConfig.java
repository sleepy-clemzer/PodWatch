package io.podwatch.web;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Sert la SPA React embarquée dans le jar (ADR-008), depuis {@code classpath:/static/}.
 *
 * <p>React Router gère ses propres routes ({@code /pods}, {@code /logs}…). Si l'utilisateur recharge
 * la page sur {@code /pods/xyz}, aucun fichier ne correspond : on renvoie alors {@code index.html}
 * et React affiche la bonne vue.
 *
 * <p>Pas de repli vers {@code index.html} pour :
 * <ul>
 *   <li>les préfixes techniques {@code api/}, {@code ws/}, {@code actuator/} : une URL d'API inconnue doit rester une 404 ;</li>
 *   <li>les chemins avec une extension ({@code .js}, {@code .css}…) : un asset manquant doit rester une 404.</li>
 * </ul>
 * Les contrôleurs REST sont toujours prioritaires sur ce gestionnaire de ressources.
 */
@Configuration
class SpaWebConfig implements WebMvcConfigurer {

    static final String INDEX_HTML = "index.html";
    private static final List<String> TECHNICAL_PREFIXES = List.of("api/", "ws/", "actuator/");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaFallbackResourceResolver());
    }

    static boolean isSpaRoute(String resourcePath) {
        boolean technical = TECHNICAL_PREFIXES.stream().anyMatch(resourcePath::startsWith)
                || TECHNICAL_PREFIXES.stream().anyMatch(p -> resourcePath.equals(p.substring(0, p.length() - 1)));
        String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        return !technical && !lastSegment.contains(".");
    }

    static class SpaFallbackResourceResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            // super.getResource conserve les contrôles de Spring (fichier lisible, situé sous la location autorisée).
            Resource requested = super.getResource(resourcePath, location);
            if (requested != null) {
                return requested;
            }
            return isSpaRoute(resourcePath) ? super.getResource(INDEX_HTML, location) : null;
        }
    }
}
