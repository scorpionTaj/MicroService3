package ma.tna.microservice3.dto;

import java.math.BigDecimal;

/**
 * DTO pour la réponse du service de tarification
 */
public record TarifResponseDTO(
        BigDecimal montant,
        String description,
        Long itineraireId
) {
}

