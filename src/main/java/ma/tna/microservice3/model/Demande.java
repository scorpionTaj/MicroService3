package ma.tna.microservice3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant une demande de transport
 */
@Entity
@Table(name = "demandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de l'utilisateur (client) provenant du Service 1
     */
    @NotNull
    @Column(nullable = false)
    private Long clientId;

    /**
     * Volume de la marchandise (en m³)
     */
    @NotNull
    @Column(nullable = false)
    private Double volume;

    /**
     * Poids de la marchandise (en kg)
     */
    @Column
    private Double poids;

    /**
     * Nature de la marchandise à transporter
     */
    @NotBlank
    @Column(nullable = false)
    private String natureMarchandise;

    /**
     * Date et heure de départ souhaitée
     */
    @NotNull
    @Column(nullable = false)
    private LocalDateTime dateDepart;

    /**
     * Ville de départ
     */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String villeDepart;

    /**
     * Ville de destination
     */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String villeDestination;

    /**
     * Statut de validation de la demande
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutValidation statutValidation = StatutValidation.EN_ATTENTE_CLIENT;

    /**
     * ID de l'itinéraire associé (UUID provenant du Service 4 - Itinéraires)
     */
    @Column(length = 100)
    private String itineraireAssocieId;

    /**
     * Distance totale calculée par le service itinéraires (en km)
     */
    @Column
    private Double distanceKm;

    /**
     * Durée totale estimée par le service itinéraires (en minutes)
     */
    @Column
    private Integer dureeEstimeeMin;

    /**
     * ID de la mission associée (pour le regroupement et l'assignation)
     */
    @Column
    private Long missionId;

    /**
     * Catégorie de la marchandise
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", referencedColumnName = "id_categorie")
    private Categorie categorie;

    /**
     * Devis estimé pour le transport
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal devisEstime;

    /**
     * Date de création de la demande
     */
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    /**
     * Date de dernière mise à jour
     */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (dateModification == null) {
            dateModification = LocalDateTime.now();
        }
    }
}

