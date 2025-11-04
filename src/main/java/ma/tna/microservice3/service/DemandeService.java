package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.dto.PaiementStatusUpdateDTO;

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
     * Met à jour le statut de paiement d'une demande
     * @param demandeId L'ID de la demande
     * @param dto Le nouveau statut de paiement
     */
    void mettreAJourStatutPaiement(Long demandeId, PaiementStatusUpdateDTO dto);

    /**
     * Récupère une demande par son ID
     * @param demandeId L'ID de la demande
     * @param userId L'ID de l'utilisateur
     * @return La demande
     */
    DemandeResponseDTO getDemandeById(Long demandeId, Long userId);

    /**
     * Récupère toutes les demandes d'un client
     * @param userId L'ID du client
     * @return Liste des demandes du client
     */
    List<DemandeResponseDTO> getDemandesByClient(Long userId);
}

