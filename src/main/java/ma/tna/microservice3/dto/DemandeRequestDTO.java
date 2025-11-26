package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * DTO pour la création d'une demande de transport
 * Utilise Java Record pour l'immuabilité
 */
@Schema(description = "Demande de transport à créer")
public record DemandeRequestDTO(
        @Schema(description = "Volume de la marchandise en m³", example = "25.5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Le volume ne peut pas être null")
        @Positive(message = "Le volume doit être positif")
        Double volume,

        @Schema(description = "Nature de la marchandise à transporter", example = "Matériaux de construction", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La nature de la marchandise est obligatoire")
        String natureMarchandise,

        @Schema(description = "Date et heure de départ souhaitée", example = "2025-12-15T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La date de départ est obligatoire")
        @Future(message = "La date de départ doit être dans le futur")
        LocalDateTime dateDepart,

        @Schema(description = "Adresse complète de départ", example = "123 Rue Mohammed V, Casablanca", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'adresse de départ est obligatoire")
        String adresseDepart,

        @Schema(description = "Adresse complète de destination", example = "456 Avenue Hassan II, Rabat", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "L'adresse de destination est obligatoire")
        String adresseDestination
) {
}

