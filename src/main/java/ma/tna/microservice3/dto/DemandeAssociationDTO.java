package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour associer une mission et un itinéraire à une demande
 */
@Schema(description = "Données pour associer une mission et un itinéraire à une demande")
public record DemandeAssociationDTO(
        @Schema(description = "ID de la mission à associer", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "L'ID de la mission est obligatoire")
        Long missionId,

        @Schema(description = "ID de l'itinéraire à associer (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String itineraireId,

        @Schema(description = "Distance en km (optionnel, mise à jour si fournie)", example = "133.7", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Double distanceKm,

        @Schema(description = "Durée estimée en minutes (optionnel, mise à jour si fournie)", example = "102", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer dureeEstimeeMin
) {
}
