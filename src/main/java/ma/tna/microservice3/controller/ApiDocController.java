package ma.tna.microservice3.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Contrôleur pour servir la documentation API personnalisée
 * Accessible sans authentification à la racine "/"
 */
@RestController
public class ApiDocController {

    /**
     * Endpoint racine qui retourne la documentation API au format JSON
     * Accessible sans authentification
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getApiDocumentation() {
        try {
            Resource resource = new ClassPathResource("api-docs.json");
            String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } catch (IOException e) {
            // Si le fichier n'existe pas, retourner une documentation par défaut
            String fallbackDoc = """
                {
                    "info": {
                        "title": "Microservice 3 - Service Demandes Transport API",
                        "description": "API de gestion des demandes de transport",
                        "version": "1.0.0",
                        "baseUrl": "/api/v1"
                    },
                    "note": "Documentation complète disponible sur /swagger-ui.html"
                }
                """;
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(fallbackDoc);
        }
    }
}
