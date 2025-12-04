package ma.tna.microservice3.repository;

import ma.tna.microservice3.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des catégories de marchandise
 */
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, String> {

    /**
     * Recherche une catégorie par son nom
     * @param nom Le nom de la catégorie
     * @return La catégorie correspondante
     */
    Optional<Categorie> findByNom(String nom);

    /**
     * Vérifie si une catégorie existe par son nom
     * @param nom Le nom de la catégorie
     * @return true si la catégorie existe
     */
    boolean existsByNom(String nom);

    /**
     * Recherche les catégories par fragilité
     * @param fragile Indicateur de fragilité
     * @return Liste des catégories correspondantes
     */
    List<Categorie> findByFragile(Boolean fragile);

    /**
     * Recherche les catégories par dangerosité
     * @param dangereux Indicateur de dangerosité
     * @return Liste des catégories correspondantes
     */
    List<Categorie> findByDangereux(Boolean dangereux);

    /**
     * Recherche les catégories par température requise
     * @param temperatureRequise La température requise
     * @return Liste des catégories correspondantes
     */
    List<Categorie> findByTemperatureRequise(String temperatureRequise);

    /**
     * Recherche les catégories contenant un mot-clé dans le nom
     * @param keyword Le mot-clé
     * @return Liste des catégories correspondantes
     */
    List<Categorie> findByNomContainingIgnoreCase(String keyword);
}
