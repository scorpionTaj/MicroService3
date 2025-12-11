package ma.tna.microservice3.repository;

import ma.tna.microservice3.model.Demande;
import ma.tna.microservice3.model.StatutValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des demandes de transport
 */
@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    /**
     * Recherche toutes les demandes d'un client
     * @param clientId L'identifiant du client
     * @return Liste des demandes du client
     */
    List<Demande> findByClientId(Long clientId);

    /**
     * Recherche les demandes par statut de validation
     * @param statutValidation Le statut de validation recherché
     * @return Liste des demandes avec ce statut
     */
    List<Demande> findByStatutValidation(StatutValidation statutValidation);

    /**
     * Recherche les demandes d'un client avec un statut spécifique
     * @param clientId L'identifiant du client
     * @param statutValidation Le statut de validation
     * @return Liste des demandes correspondantes
     */
    List<Demande> findByClientIdAndStatutValidation(Long clientId, StatutValidation statutValidation);

    /**
     * Recherche les demandes par ID de mission
     * @param missionId L'identifiant de la mission
     * @return Liste des demandes de cette mission
     */
    List<Demande> findByMissionId(Long missionId);
}

