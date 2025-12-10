package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'une demande de transport
 * Utilise Java Record pour l'immuabilité
 */
@Schema(description = "Réponse contenant les détails d'une demande de transport")
public record DemandeResponseDTO(
        @Schema(description = "Identifiant unique de la demande", example = "1")
        Long id,

        @Schema(description = "Identifiant du client", example = "42")
        Long clientId,

        @Schema(description = "Volume de la marchandise en m³", example = "25.5")
        Double volume,

        @Schema(description = "Poids de la marchandise en kg", example = "500.0", nullable = true)
        Double poids,

        @Schema(description = "Nature de la marchandise", example = "Matériaux de construction")
        String natureMarchandise,

        @Schema(description = "Date et heure de départ", example = "2025-12-15T08:00:00")
        LocalDateTime dateDepart,

        @Schema(description = "Ville de départ", example = "Casablanca")
        String villeDepart,

        @Schema(description = "Ville de destination", example = "Rabat")
        String villeDestination,

        @Schema(description = "Statut de validation", example = "EN_ATTENTE_CLIENT", allowableValues = {"EN_ATTENTE_CLIENT", "VALIDEE", "REFUSEE", "EN_COURS", "TERMINEE"})
        String statutValidation,

        @Schema(description = "Devis estimé en MAD", example = "1500.00")
        BigDecimal devisEstime,

        @Schema(description = "ID de l'itinéraire associé (UUID du service Itinéraires)", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
        String itineraireAssocieId,

        @Schema(description = "Distance totale en kilomètres", example = "133.7", nullable = true)
        Double distanceKm,

        @Schema(description = "Durée estimée en minutes", example = "102", nullable = true)
        Integer dureeEstimeeMin,

        @Schema(description = "ID de la mission associée", example = "3", nullable = true)
        Long missionId,

        @Schema(description = "Informations sur la catégorie de marchandise", nullable = true)
        CategorieResponseDTO categorie,

        @Schema(description = "Date de création de la demande", example = "2025-11-26T10:30:00")
        LocalDateTime dateCreation,

        @Schema(description = "Date de dernière modification", example = "2025-11-26T14:45:00")
        LocalDateTime dateModification
) {
}

