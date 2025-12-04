package ma.tna.microservice3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.service.CategorieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des catégories de marchandise
 */
@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Catégories de Marchandise", description = "API de gestion des catégories de marchandise")
public class CategorieController {

    private static final Logger logger = LoggerFactory.getLogger(CategorieController.class);

    private final CategorieService categorieService;

    public CategorieController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    /**
     * Crée une nouvelle catégorie
     */
    @Operation(
        summary = "Créer une nouvelle catégorie",
        description = "Crée une nouvelle catégorie de marchandise avec ses caractéristiques"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Catégorie créée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorieResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "409", description = "Une catégorie avec ce nom existe déjà")
    })
    @PostMapping
    public ResponseEntity<CategorieResponseDTO> creerCategorie(
            @Valid @RequestBody CategorieRequestDTO requestDTO
    ) {
        logger.info("Création d'une nouvelle catégorie: {}", requestDTO.nom());
        CategorieResponseDTO response = categorieService.creerCategorie(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Met à jour une catégorie existante
     */
    @Operation(
        summary = "Mettre à jour une catégorie",
        description = "Met à jour les informations d'une catégorie existante"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie mise à jour avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorieResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée"),
        @ApiResponse(responseCode = "409", description = "Une catégorie avec ce nom existe déjà")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategorieResponseDTO> mettreAJourCategorie(
            @Parameter(description = "ID de la catégorie (UUID)", required = true)
            @PathVariable String id,
            @Valid @RequestBody CategorieRequestDTO requestDTO
    ) {
        logger.info("Mise à jour de la catégorie ID: {}", id);
        CategorieResponseDTO response = categorieService.mettreAJourCategorie(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère une catégorie par son ID
     */
    @Operation(
        summary = "Récupérer une catégorie par ID",
        description = "Retourne les détails complets d'une catégorie spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie récupérée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorieResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategorieResponseDTO> getCategorieById(
            @Parameter(description = "ID de la catégorie (UUID)", required = true)
            @PathVariable String id
    ) {
        logger.info("Récupération de la catégorie ID: {}", id);
        CategorieResponseDTO response = categorieService.getCategorieById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère toutes les catégories
     */
    @Operation(
        summary = "Récupérer toutes les catégories",
        description = "Retourne la liste de toutes les catégories de marchandise"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories récupérée avec succès")
    })
    @GetMapping
    public ResponseEntity<List<CategorieResponseDTO>> getAllCategories() {
        logger.info("Récupération de toutes les catégories");
        List<CategorieResponseDTO> categories = categorieService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Recherche une catégorie par son nom
     */
    @Operation(
        summary = "Rechercher une catégorie par nom",
        description = "Retourne la catégorie correspondant au nom exact"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catégorie trouvée",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategorieResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @GetMapping("/nom/{nom}")
    public ResponseEntity<CategorieResponseDTO> getCategorieByNom(
            @Parameter(description = "Nom de la catégorie", required = true)
            @PathVariable String nom
    ) {
        logger.info("Recherche de la catégorie par nom: {}", nom);
        CategorieResponseDTO response = categorieService.getCategorieByNom(nom);
        return ResponseEntity.ok(response);
    }

    /**
     * Recherche les catégories par mot-clé
     */
    @Operation(
        summary = "Rechercher des catégories par mot-clé",
        description = "Retourne les catégories dont le nom contient le mot-clé"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories correspondantes")
    })
    @GetMapping("/search")
    public ResponseEntity<List<CategorieResponseDTO>> searchCategories(
            @Parameter(description = "Mot-clé de recherche", required = true)
            @RequestParam String keyword
    ) {
        logger.info("Recherche de catégories avec mot-clé: {}", keyword);
        List<CategorieResponseDTO> categories = categorieService.searchCategories(keyword);
        return ResponseEntity.ok(categories);
    }

    /**
     * Filtre les catégories par fragilité
     */
    @Operation(
        summary = "Filtrer les catégories par fragilité",
        description = "Retourne les catégories selon leur caractère fragile"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories correspondantes")
    })
    @GetMapping("/filter/fragile")
    public ResponseEntity<List<CategorieResponseDTO>> getCategoriesByFragile(
            @Parameter(description = "Filtrer par fragilité (true/false)", required = true)
            @RequestParam Boolean fragile
    ) {
        logger.info("Filtrage des catégories par fragilité: {}", fragile);
        List<CategorieResponseDTO> categories = categorieService.getCategoriesByFragile(fragile);
        return ResponseEntity.ok(categories);
    }

    /**
     * Filtre les catégories par dangerosité
     */
    @Operation(
        summary = "Filtrer les catégories par dangerosité",
        description = "Retourne les catégories selon leur caractère dangereux"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories correspondantes")
    })
    @GetMapping("/filter/dangereux")
    public ResponseEntity<List<CategorieResponseDTO>> getCategoriesByDangereux(
            @Parameter(description = "Filtrer par dangerosité (true/false)", required = true)
            @RequestParam Boolean dangereux
    ) {
        logger.info("Filtrage des catégories par dangerosité: {}", dangereux);
        List<CategorieResponseDTO> categories = categorieService.getCategoriesByDangereux(dangereux);
        return ResponseEntity.ok(categories);
    }

    /**
     * Filtre les catégories par température requise
     */
    @Operation(
        summary = "Filtrer les catégories par température",
        description = "Retourne les catégories selon la température requise pour le transport"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des catégories correspondantes")
    })
    @GetMapping("/filter/temperature")
    public ResponseEntity<List<CategorieResponseDTO>> getCategoriesByTemperature(
            @Parameter(description = "Température requise (ambiante, refrigere, congele)", required = true)
            @RequestParam String temperature
    ) {
        logger.info("Filtrage des catégories par température: {}", temperature);
        List<CategorieResponseDTO> categories = categorieService.getCategoriesByTemperature(temperature);
        return ResponseEntity.ok(categories);
    }

    /**
     * Supprime une catégorie
     */
    @Operation(
        summary = "Supprimer une catégorie",
        description = "Supprime définitivement une catégorie"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Catégorie supprimée avec succès"),
        @ApiResponse(responseCode = "404", description = "Catégorie non trouvée")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCategorie(
            @Parameter(description = "ID de la catégorie (UUID)", required = true)
            @PathVariable String id
    ) {
        logger.info("Suppression de la catégorie ID: {}", id);
        categorieService.supprimerCategorie(id);
        return ResponseEntity.noContent().build();
    }
}
