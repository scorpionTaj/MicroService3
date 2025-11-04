package ma.tna.microservice3.dto;

/**
 * DTO pour la réponse du service d'itinéraires
 */
public record ItineraireResponseDTO(
        Long id,
        String pointDepart,
        String pointArrivee,
        Double distance,
        Integer dureeEstimee
) {
}

