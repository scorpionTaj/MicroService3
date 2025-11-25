package ma.tna.microservice3.controller;

import jakarta.validation.Valid;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.dto.PaiementStatusUpdateDTO;
import ma.tna.microservice3.service.DemandeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des demandes de transport
 */
@RestController
@RequestMapping("/api/v1/demandes")
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
    @PutMapping("/{id}/validation")
    public ResponseEntity<DemandeResponseDTO> validerDemande(
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
    @GetMapping
    public ResponseEntity<List<DemandeResponseDTO>> getMesDemandesClient() {
        Long userId = getCurrentUserId();
        logger.info("Récupération des demandes du client ID: {}", userId);

        List<DemandeResponseDTO> demandes = demandeService.getDemandesByClient(userId);

        return ResponseEntity.ok(demandes);
    }

    /**
     * Récupère une demande spécifique par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponseDTO> getDemandeById(
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId();
        logger.info("Récupération de la demande ID: {} par l'utilisateur ID: {}", id, userId);

        DemandeResponseDTO response = demandeService.getDemandeById(id, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * Webhook pour la mise à jour du statut de paiement
     * Appelé par le Service Paiements
     * Note: Cette route devrait être sécurisée par une clé API ou restriction IP en production
     */
    @PutMapping("/{id}/paiement")
    public ResponseEntity<Void> mettreAJourStatutPaiement(
            @PathVariable Long id,
            @Valid @RequestBody PaiementStatusUpdateDTO statusUpdateDTO
    ) {
        logger.info("Mise à jour du statut de paiement pour la demande ID: {}", id);

        demandeService.mettreAJourStatutPaiement(id, statusUpdateDTO);

        return ResponseEntity.ok().build();
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
}

