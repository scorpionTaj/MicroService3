package ma.tna.microservice3.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/**
 * DTO pour la création d'une demande de transport
 * Utilise Java Record pour l'immuabilité
 */
public record DemandeRequestDTO(
        @NotNull(message = "Le volume ne peut pas être null")
        @Positive(message = "Le volume doit être positif")
        Double volume,

        @NotBlank(message = "La nature de la marchandise est obligatoire")
        String natureMarchandise,

        @NotNull(message = "La date de départ est obligatoire")
        @Future(message = "La date de départ doit être dans le futur")
        LocalDateTime dateDepart,

        @NotBlank(message = "L'adresse de départ est obligatoire")
        String adresseDepart,

        @NotBlank(message = "L'adresse de destination est obligatoire")
        String adresseDestination
) {
}

