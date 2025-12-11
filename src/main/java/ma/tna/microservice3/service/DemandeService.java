package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.DemandeAssociationDTO;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;

import java.util.List;

/**
 * Interface du service de gestion des demandes de transport
 */
public interface DemandeService {

    /**
     * Crée une nouvelle demande de transport
     * @param dto Les données de la demande
     * @param userId L'ID de l'utilisateur (client)
     * @return La demande créée avec le devis et l'itinéraire
     */
    DemandeResponseDTO creerDemande(DemandeRequestDTO dto, Long userId);

    /**
     * Valide une demande par le client
     * @param demandeId L'ID de la demande
     * @param userId L'ID de l'utilisateur (client)
     * @return La demande validée
     */
    DemandeResponseDTO validerDemandeClient(Long demandeId, Long userId);

    /**
     * Récupère une demande par son ID (avec vérification des droits selon le rôle)
     * @param demandeId L'ID de la demande
     * @param userId L'ID de l'utilisateur
     * @param role Le rôle de l'utilisateur (CLIENT, ADMIN, PRESTATAIRE)
     * @return La demande
     */
    DemandeResponseDTO getDemandeById(Long demandeId, Long userId, String role);

    /**
     * Récupère toutes les demandes d'un client
     * @param userId L'ID du client
     * @return Liste des demandes du client
     */
    List<DemandeResponseDTO> getDemandesByClient(Long userId);

    /**
     * Récupère TOUTES les demandes (Admin uniquement)
     * @return Liste de toutes les demandes
     */
    List<DemandeResponseDTO> getAllDemandes();

    /**
     * Récupère les demandes par statut (Admin uniquement)
     * @param statut Le statut de validation
     * @return Liste des demandes avec ce statut
     */
    List<DemandeResponseDTO> getDemandesByStatut(String statut);

    /**
     * Récupère les demandes associées à une mission (pour Prestataire/Admin)
     * @param missionId L'ID de la mission
     * @return Liste des demandes de cette mission
     */
    List<DemandeResponseDTO> getDemandesByMission(Long missionId);

    /**
     * Associe une mission et un itinéraire à une demande
     * @param demandeId L'ID de la demande
     * @param associationDTO Les données d'association (missionId, itineraireId, etc.)
     * @return La demande mise à jour
     */
    DemandeResponseDTO associerDemande(Long demandeId, DemandeAssociationDTO associationDTO);

    /**
     * Met à jour le statut d'une demande (Admin uniquement)
     * @param demandeId L'ID de la demande
     * @param nouveauStatut Le nouveau statut
     * @return La demande mise à jour
     */
    DemandeResponseDTO updateStatut(Long demandeId, String nouveauStatut);
}

