package ma.tna.microservice3.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ma.tna.microservice3.dto.ClientInfoDTO;
import ma.tna.microservice3.dto.DemandeAssociationDTO;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.service.DemandeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des demandes de transport
 */
@RestController
@RequestMapping("/api/v1/demandes")
@Tag(name = "Demandes de Transport", description = "API de gestion des demandes de transport")
public class DemandeController {

    private static final Logger logger = LoggerFactory.getLogger(DemandeController.class);

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    /**
     * Crée une nouvelle demande de transport
     * Accessible uniquement aux clients authentifiés
     */
    @Operation(
        summary = "Créer une nouvelle demande de transport",
        description = "Crée une nouvelle demande de transport pour le client authentifié avec calcul automatique du devis",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Demande créée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemandeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PostMapping
    public ResponseEntity<DemandeResponseDTO> creerDemande(
            @Valid @RequestBody DemandeRequestDTO requestDTO
    ) {
        Long userId = getCurrentUserId();
        logger.info("Création d'une demande par le client ID: {}", userId);

        DemandeResponseDTO response = demandeService.creerDemande(requestDTO, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Valide une demande (par le client)
     * Le client confirme qu'il accepte le devis
     */
    @Operation(
        summary = "Valider une demande",
        description = "Le client confirme qu'il accepte le devis de la demande",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Demande validée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemandeResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    @PutMapping("/{id}/validation")
    public ResponseEntity<DemandeResponseDTO> validerDemande(
            @Parameter(description = "ID de la demande à valider", required = true)
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        logger.info("Validation de la demande ID: {} par le client ID: {}", id, userId);

        DemandeResponseDTO response = demandeService.validerDemandeClient(id, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère toutes les demandes du client authentifié
     */
    @Operation(
        summary = "Récupérer toutes les demandes du client",
        description = "Retourne la liste de toutes les demandes de transport du client authentifié",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des demandes récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @GetMapping
    public ResponseEntity<List<DemandeResponseDTO>> getMesDemandesClient() {
        Long userId = getCurrentUserId();
        logger.info("Récupération des demandes du client ID: {}", userId);

        List<DemandeResponseDTO> demandes = demandeService.getDemandesByClient(userId);

        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupère TOUTES les demandes (Admin uniquement)
     */
    @Operation(
        summary = "Récupérer toutes les demandes (Admin)",
        description = "Retourne la liste de toutes les demandes de transport. Accessible uniquement aux administrateurs.",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste de toutes les demandes récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<List<DemandeResponseDTO>> getAllDemandes() {
        Long userId = getCurrentUserId();
        logger.info("Récupération de TOUTES les demandes par l'admin ID: {}", userId);

        List<DemandeResponseDTO> demandes = demandeService.getAllDemandes();

        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupère les demandes par mission ID (Prestataire)
     */
    @Operation(
        summary = "Récupérer les demandes par mission (Prestataire)",
        description = "Retourne la liste des demandes associées à une mission spécifique. Accessible aux prestataires et administrateurs.",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des demandes de la mission récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @PreAuthorize("hasAnyRole('PRESTATAIRE', 'ADMIN')")
    @GetMapping("/mission/{missionId}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesByMission(
            @Parameter(description = "ID de la mission", required = true)
            @PathVariable Long missionId
    ) {
        Long userId = getCurrentUserId();
        logger.info("Récupération des demandes de la mission ID: {} par l'utilisateur ID: {}", missionId, userId);

        List<DemandeResponseDTO> demandes = demandeService.getDemandesByMission(missionId);

        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupère les demandes par statut (Admin)
     */
    @Operation(
        summary = "Récupérer les demandes par statut (Admin)",
        description = "Retourne la liste des demandes ayant un statut spécifique. Accessible uniquement aux administrateurs.",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des demandes par statut récupérée avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé - Réservé aux administrateurs")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/statut/{statut}")
    public ResponseEntity<List<DemandeResponseDTO>> getDemandesByStatut(
            @Parameter(description = "Statut de validation (EN_ATTENTE_CLIENT, VALIDEE_CLIENT, REFUSEE, etc.)", required = true)
            @PathVariable String statut
    ) {
        logger.info("Récupération des demandes avec statut: {}", statut);

        List<DemandeResponseDTO> demandes = demandeService.getDemandesByStatut(statut);

        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupère une demande spécifique par son ID
     */
    @Operation(
        summary = "Récupérer une demande par ID",
        description = "Retourne les détails complets d'une demande spécifique",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Demande récupérée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemandeResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponseDTO> getDemandeById(
            @Parameter(description = "ID de la demande", required = true)
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        logger.info("Récupération de la demande ID: {} par l'utilisateur ID: {} avec rôle: {}", id, userId, role);

        DemandeResponseDTO response = demandeService.getDemandeById(id, userId, role);

        return ResponseEntity.ok(response);
    }

    /**
     * Associe une mission et un itinéraire à une demande
     * Permet de mettre à jour le missionId et itineraireId d'une demande existante
     */
    @Operation(
        summary = "Associer une mission et un itinéraire à une demande",
        description = "Met à jour une demande avec l'ID de mission et l'ID d'itinéraire associés",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Demande associée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DemandeResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides"),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    @PutMapping("/{id}/association")
    public ResponseEntity<DemandeResponseDTO> associerDemande(
            @Parameter(description = "ID de la demande à associer", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DemandeAssociationDTO associationDTO
    ) {
        logger.info("Association de la demande ID: {} avec mission ID: {} et itinéraire ID: {}",
                id, associationDTO.missionId(), associationDTO.itineraireAssocieId());

        DemandeResponseDTO response = demandeService.associerDemande(id, associationDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les informations du client associé à une demande
     * Appelle le Service Utilisateurs pour obtenir les détails
     */
    @Operation(
        summary = "Récupérer les informations du client d'une demande",
        description = "Retourne les informations du client (nom, email, téléphone) associé à une demande. " +
                      "Appelle le Service Utilisateurs en interne.",
        security = {@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Informations du client récupérées avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientInfoDTO.class))),
        @ApiResponse(responseCode = "401", description = "Non authentifié"),
        @ApiResponse(responseCode = "403", description = "Accès refusé"),
        @ApiResponse(responseCode = "404", description = "Demande ou client non trouvé")
    })
    @GetMapping("/{id}/client")
    public ResponseEntity<ClientInfoDTO> getClientInfoByDemande(
            @Parameter(description = "ID de la demande", required = true)
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = getCurrentUserId();
        String role = getCurrentUserRole();
        logger.info("Récupération des infos client pour la demande ID: {} par l'utilisateur ID: {} avec rôle: {}", 
                id, userId, role);

        ClientInfoDTO clientInfo = demandeService.getClientInfoByDemande(id, userId, role, authHeader);

        return ResponseEntity.ok(clientInfo);
    }

    /**
     * Récupère l'ID de l'utilisateur actuellement authentifié
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    logger.warn("Impossible de convertir le principal (String) en Long: {}", principal);
                }
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                try {
                    return Long.parseLong(username);
                } catch (NumberFormatException e) {
                    logger.warn("Impossible de convertir le username en Long: {}", username);
                }
            }
        }

        throw new RuntimeException("Utilisateur non authentifié");
    }

    /**
     * Récupère le rôle de l'utilisateur actuellement authentifié
     */
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();
                if (role.startsWith("ROLE_")) {
                    return role.substring(5); // Remove "ROLE_" prefix
                }
                return role;
            }
        }

        return "CLIENT"; // Default role
    }
}

