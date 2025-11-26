package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ma.tna.microservice3.model.StatutPaiement;

/**
 * DTO pour la mise à jour du statut de paiement
 * Utilise Java Record pour l'immuabilité
 */
@Schema(description = "Mise à jour du statut de paiement d'une demande")
public record PaiementStatusUpdateDTO(
        @Schema(description = "Nouveau statut de paiement", example = "PAYEE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le nouveau statut de paiement est obligatoire")
        StatutPaiement nouveauStatut
) {
}

