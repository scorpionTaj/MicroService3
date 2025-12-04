package ma.tna.microservice3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO pour la création/mise à jour d'une catégorie de marchandise
 */
@Schema(description = "Catégorie de marchandise à créer ou modifier")
public record CategorieRequestDTO(
        @Schema(description = "Nom de la catégorie", example = "Produits Alimentaires", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String nom,

        @Schema(description = "Description détaillée de la catégorie", example = "Produits alimentaires nécessitant une chaîne de froid")
        String description,

        @Schema(description = "Densité moyenne en kg/m³", example = "850.0")
        @PositiveOrZero(message = "La densité moyenne doit être positive ou nulle")
        Double densiteMoyenne,

        @Schema(description = "Indique si la marchandise est fragile", example = "false")
        Boolean fragile,

        @Schema(description = "Indique si la marchandise est dangereuse", example = "false")
        Boolean dangereux,

        @Schema(description = "Température requise pour le transport", example = "refrigere", allowableValues = {"ambiante", "refrigere", "congele"})
        String temperatureRequise,

        @Schema(description = "Restrictions spéciales pour le transport", example = "Ne pas empiler plus de 3 caisses")
        String restrictions
) {
}
