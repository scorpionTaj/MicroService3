package ma.tna.microservice3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO pour la réponse du service de tarification
 */
@Schema(description = "Tarif calculé pour le transport")
@JsonIgnoreProperties(ignoreUnknown = true)
public record TarifResponseDTO(
        @Schema(description = "Montant du tarif en MAD", example = "1500.00")
        BigDecimal montant,

        @Schema(description = "Description du tarif", example = "Tarif standard pour 87.5 km")
        String description,

        @Schema(description = "ID de l'itinéraire associé (UUID)", example = "550e8400-e29b-41d4-a716-446655440000", nullable = true)
        String itineraireId
) {
}

