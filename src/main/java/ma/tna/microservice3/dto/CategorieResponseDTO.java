package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'une catégorie de marchandise
 */
@Schema(description = "Réponse contenant les détails d'une catégorie de marchandise")
public record CategorieResponseDTO(
        @Schema(description = "Identifiant unique de la catégorie (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        String idCategorie,

        @Schema(description = "Nom de la catégorie", example = "Produits Alimentaires")
        String nom,

        @Schema(description = "Description détaillée de la catégorie", example = "Produits alimentaires nécessitant une chaîne de froid")
        String description,

        @Schema(description = "Densité moyenne en kg/m³", example = "850.0")
        Double densiteMoyenne,

        @Schema(description = "Indique si la marchandise est fragile", example = "false")
        Boolean fragile,

        @Schema(description = "Indique si la marchandise est dangereuse", example = "false")
        Boolean dangereux,

        @Schema(description = "Température requise pour le transport", example = "refrigere")
        String temperatureRequise,

        @Schema(description = "Restrictions spéciales pour le transport", example = "Ne pas empiler plus de 3 caisses")
        String restrictions,

        @Schema(description = "Date de création de la catégorie", example = "2025-11-26T10:30:00")
        LocalDateTime dateCreation,

        @Schema(description = "Date de dernière modification", example = "2025-11-26T14:45:00")
        LocalDateTime dateModification
) {
}
