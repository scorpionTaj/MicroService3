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

        @Schema(description = "Nature de la marchandise", example = "Matériaux de construction")
        String natureMarchandise,

        @Schema(description = "Date et heure de départ", example = "2025-12-15T08:00:00")
        LocalDateTime dateDepart,

        @Schema(description = "Adresse de départ", example = "123 Rue Mohammed V, Casablanca")
        String adresseDepart,

        @Schema(description = "Adresse de destination", example = "456 Avenue Hassan II, Rabat")
        String adresseDestination,

        @Schema(description = "Statut de validation", example = "EN_ATTENTE", allowableValues = {"EN_ATTENTE", "VALIDEE", "REFUSEE"})
        String statutValidation,

        @Schema(description = "Devis estimé en MAD", example = "1500.00")
        BigDecimal devisEstime,

        @Schema(description = "ID de l'itinéraire associé", example = "5", nullable = true)
        Long itineraireAssocieId,

        @Schema(description = "ID du groupe de transport", example = "3", nullable = true)
        Long groupeId,

        @Schema(description = "Informations sur la catégorie de marchandise", nullable = true)
        CategorieResponseDTO categorie,

        @Schema(description = "Date de création de la demande", example = "2025-11-26T10:30:00")
        LocalDateTime dateCreation,

        @Schema(description = "Date de dernière modification", example = "2025-11-26T14:45:00")
        LocalDateTime dateModification
) {
}

