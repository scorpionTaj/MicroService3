package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO pour la réponse du service de tarification
 */
@Schema(description = "Tarif calculé pour l'itinéraire")
public record TarifResponseDTO(
        @Schema(description = "Montant du tarif en MAD", example = "1500.00")
        BigDecimal montant,

        @Schema(description = "Description du tarif", example = "Tarif standard pour 87.5 km")
        String description,

        @Schema(description = "ID de l'itinéraire associé", example = "5")
        Long itineraireId
) {
}

