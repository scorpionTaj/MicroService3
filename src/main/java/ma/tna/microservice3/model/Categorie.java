package ma.tna.microservice3.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant une catégorie de marchandise
 * Permet de classifier les marchandises avec leurs caractéristiques spécifiques
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_categorie", length = 36)
    private String idCategorie;

    /**
     * Nom de la catégorie
     */
    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    /**
     * Description détaillée de la catégorie
     */
    @Column(length = 500)
    private String description;

    /**
     * Densité moyenne de la marchandise (kg/m³)
     */
    @PositiveOrZero
    @Column(name = "densite_moyenne")
    private Double densiteMoyenne;

    /**
     * Indique si la marchandise est fragile
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean fragile = false;

    /**
     * Indique si la marchandise est dangereuse
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean dangereux = false;

    /**
     * Température requise pour le transport
     * Valeurs possibles: "ambiante", "refrigere", "congele"
     */
    @Column(name = "temperature_requise", length = 50)
    @Builder.Default
    private String temperatureRequise = "ambiante";

    /**
     * Restrictions spéciales pour le transport
     */
    @Column(length = 500)
    private String restrictions;

    /**
     * Date de création de la catégorie
     */
    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    /**
     * Date de dernière mise à jour
     */
    @Column(name = "date_modification", nullable = false)
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
