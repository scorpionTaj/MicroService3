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

        @Schema(description = "Poids de la marchandise en kg", example = "500.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Positive(message = "Le poids doit être positif")
        Double poids,

        @Schema(description = "Nature de la marchandise à transporter", example = "Matériaux de construction", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La nature de la marchandise est obligatoire")
        String natureMarchandise,

        @Schema(description = "Date et heure de départ souhaitée", example = "2025-12-15T08:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La date de départ est obligatoire")
        @Future(message = "La date de départ doit être dans le futur")
        LocalDateTime dateDepart,

        @Schema(description = "Ville de départ", example = "Casablanca", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La ville de départ est obligatoire")
        String villeDepart,

        @Schema(description = "Ville de destination", example = "Rabat", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La ville de destination est obligatoire")
        String villeDestination,

        @Schema(description = "ID de la catégorie de marchandise (UUID)", example = "cat-001-meubles", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String categorieId
) {
}

