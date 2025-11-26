package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour la réponse du service d'itinéraires
 */
@Schema(description = "Itinéraire calculé pour la demande")
public record ItineraireResponseDTO(
        @Schema(description = "Identifiant de l'itinéraire", example = "5")
        Long id,

        @Schema(description = "Point de départ", example = "Casablanca")
        String pointDepart,

        @Schema(description = "Point d'arrivée", example = "Rabat")
        String pointArrivee,

        @Schema(description = "Distance en kilomètres", example = "87.5")
        Double distance,

        @Schema(description = "Durée estimée en minutes", example = "90")
        Integer dureeEstimee
) {
}

