package ma.tna.microservice3.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour la documentation de l'API
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservice 3 - Service Demandes Transport API")
                        .description("API de gestion des demandes de transport pour le système de transport.\n\n" +
                                "Cette API permet aux clients de créer des demandes de transport, de les valider, " +
                                "et de suivre leur statut de paiement. Elle communique avec les services Itinéraires " +
                                "et Tarifs pour calculer automatiquement les devis.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TNA Transport")
                                .email("support@tna.ma")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Serveur de développement local")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu depuis le service d'authentification (MicroService1)")));
    }
}
