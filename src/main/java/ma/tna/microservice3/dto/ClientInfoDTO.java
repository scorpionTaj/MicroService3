package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour les informations du client provenant du Service Utilisateurs
 */
@Schema(description = "Informations du client")
public record ClientInfoDTO(
        @Schema(description = "ID du client", example = "1")
        Long id,

        @Schema(description = "Email du client", example = "client@example.com")
        String email,

        @Schema(description = "Nom complet du client", example = "Ahmed Bennani")
        String nom,

        @Schema(description = "Prénom du client", example = "Ahmed")
        String prenom,

        @Schema(description = "Numéro de téléphone", example = "+212612345678")
        String telephone,

        @Schema(description = "Type d'utilisateur", example = "CLIENT")
        String userType
) {
}
