package ma.tna.microservice3.dto;

import jakarta.validation.constraints.NotNull;
import ma.tna.microservice3.model.StatutPaiement;

/**
 * DTO pour la mise à jour du statut de paiement
 * Utilise Java Record pour l'immuabilité
 */
public record PaiementStatusUpdateDTO(
        @NotNull(message = "Le nouveau statut de paiement est obligatoire")
        StatutPaiement nouveauStatut
) {
}

