package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;

import java.util.List;

/**
 * Interface du service de gestion des catégories de marchandise
 */
public interface CategorieService {

    /**
     * Crée une nouvelle catégorie
     * @param dto Les données de la catégorie
     * @return La catégorie créée
     */
    CategorieResponseDTO creerCategorie(CategorieRequestDTO dto);

    /**
     * Met à jour une catégorie existante
     * @param id L'ID de la catégorie
     * @param dto Les nouvelles données
     * @return La catégorie mise à jour
     */
    CategorieResponseDTO mettreAJourCategorie(String id, CategorieRequestDTO dto);

    /**
     * Récupère une catégorie par son ID
     * @param id L'ID de la catégorie
     * @return La catégorie
     */
    CategorieResponseDTO getCategorieById(String id);

    /**
     * Récupère une catégorie par son nom
     * @param nom Le nom de la catégorie
     * @return La catégorie
     */
    CategorieResponseDTO getCategorieByNom(String nom);

    /**
     * Récupère toutes les catégories
     * @return Liste de toutes les catégories
     */
    List<CategorieResponseDTO> getAllCategories();

    /**
     * Recherche les catégories par fragilité
     * @param fragile Indicateur de fragilité
     * @return Liste des catégories correspondantes
     */
    List<CategorieResponseDTO> getCategoriesByFragile(Boolean fragile);

    /**
     * Recherche les catégories par dangerosité
     * @param dangereux Indicateur de dangerosité
     * @return Liste des catégories correspondantes
     */
    List<CategorieResponseDTO> getCategoriesByDangereux(Boolean dangereux);

    /**
     * Recherche les catégories par température requise
     * @param temperatureRequise La température requise
     * @return Liste des catégories correspondantes
     */
    List<CategorieResponseDTO> getCategoriesByTemperature(String temperatureRequise);

    /**
     * Recherche les catégories par mot-clé dans le nom
     * @param keyword Le mot-clé
     * @return Liste des catégories correspondantes
     */
    List<CategorieResponseDTO> searchCategories(String keyword);

    /**
     * Supprime une catégorie
     * @param id L'ID de la catégorie à supprimer
     */
    void supprimerCategorie(String id);
}
