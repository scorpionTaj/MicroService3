package ma.tna.microservice3.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'une demande de transport
 * Utilise Java Record pour l'immuabilité
 */
public record DemandeResponseDTO(
        Long id,
        Long clientId,
        Double volume,
        String natureMarchandise,
        LocalDateTime dateDepart,
        String adresseDepart,
        String adresseDestination,
        String statutValidation,
        String statutPaiement,
        BigDecimal devisEstime,
        Long itineraireAssocieId,
        Long groupeId,
        LocalDateTime dateCreation,
        LocalDateTime dateModification
) {
}

