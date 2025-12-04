package ma.tna.microservice3.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO pour la réponse du service d'itinéraires (MS4)
 * Correspond à UserRouteInfoDTO du service Itinéraires
 */
@Schema(description = "Itinéraire calculé pour la demande")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItineraireResponseDTO(
        @Schema(description = "Identifiant de l'itinéraire (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        String routeId,

        @Schema(description = "Adresse de départ", example = "123 Rue Mohammed V, Casablanca")
        String adresseDepart,

        @Schema(description = "Adresse de destination", example = "456 Avenue Hassan II, Rabat")
        String adresseDestination,

        @Schema(description = "Ville de départ", example = "Casablanca")
        String originCity,

        @Schema(description = "Ville de destination", example = "Rabat")
        String destinationCity,

        @Schema(description = "Latitude du point de départ", example = "33.5731")
        Double originLatitude,

        @Schema(description = "Longitude du point de départ", example = "-7.5898")
        Double originLongitude,

        @Schema(description = "Latitude de destination", example = "34.0209")
        Double destinationLatitude,

        @Schema(description = "Longitude de destination", example = "-6.8416")
        Double destinationLongitude,

        @Schema(description = "Distance totale en kilomètres (aller + retour si applicable)", example = "133.7")
        Double totalDistanceKm,

        @Schema(description = "Durée totale estimée en minutes", example = "102")
        Integer totalDurationMin,

        @Schema(description = "Distance aller en kilomètres", example = "66.85")
        Double distanceKm,

        @Schema(description = "Durée aller estimée en minutes", example = "51")
        Integer durationMin,

        @Schema(description = "Distance retour en kilomètres", example = "66.85")
        Double returnDistanceKm,

        @Schema(description = "Durée retour estimée en minutes", example = "51")
        Integer returnDurationMin,

        @Schema(description = "Inclut le trajet retour", example = "true")
        Boolean includeReturn,

        @Schema(description = "Itinéraire optimisé", example = "false")
        Boolean isOptimized,

        @Schema(description = "Statut du calcul", example = "SUCCESS")
        String status,

        @Schema(description = "Date de création", example = "2025-12-03 14:30:00")
        String createdAt
) {
}

