package io.podwatch.system;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Informations publiques sur l'application (nom, version).
 * Utilisé par le frontend pour afficher la version et vérifier que le backend répond.
 */
@RestController
@RequestMapping("/api/v1/system")
class SystemInfoController {

    static final String DEV_VERSION = "dev";

    private final SystemInfo systemInfo;

    SystemInfoController(@Value("${spring.application.name}") String applicationName,
                         ObjectProvider<BuildProperties> buildProperties) {
        // BuildProperties est généré par le goal Maven "build-info" ; absent en exécution depuis l'IDE.
        String version = buildProperties.stream().map(BuildProperties::getVersion).findFirst().orElse(DEV_VERSION);
        this.systemInfo = new SystemInfo(applicationName, version);
    }

    @GetMapping("/info")
    SystemInfo info() {
        return systemInfo;
    }

    /** DTO exposé. Ne jamais y ajouter d'information sensible : l'endpoint est public. */
    record SystemInfo(String name, String version) {
    }
}
