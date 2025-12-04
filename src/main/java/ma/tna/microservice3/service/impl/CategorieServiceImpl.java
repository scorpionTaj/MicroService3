package ma.tna.microservice3.service.impl;

import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.exception.ResourceNotFoundException;
import ma.tna.microservice3.mapper.CategorieMapper;
import ma.tna.microservice3.model.Categorie;
import ma.tna.microservice3.repository.CategorieRepository;
import ma.tna.microservice3.service.CategorieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des catégories
 */
@Service
@Transactional
public class CategorieServiceImpl implements CategorieService {

    private static final Logger logger = LoggerFactory.getLogger(CategorieServiceImpl.class);

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    public CategorieServiceImpl(CategorieRepository categorieRepository, CategorieMapper categorieMapper) {
        this.categorieRepository = categorieRepository;
        this.categorieMapper = categorieMapper;
    }

    @Override
    public CategorieResponseDTO creerCategorie(CategorieRequestDTO dto) {
        logger.info("Création d'une nouvelle catégorie: {}", dto.nom());

        // Vérifier si une catégorie avec ce nom existe déjà
        if (categorieRepository.existsByNom(dto.nom())) {
            throw new IllegalArgumentException("Une catégorie avec le nom '" + dto.nom() + "' existe déjà");
        }

        Categorie categorie = categorieMapper.toEntity(dto);
        Categorie savedCategorie = categorieRepository.save(categorie);

        logger.info("Catégorie créée avec ID: {}", savedCategorie.getIdCategorie());
        return categorieMapper.toResponseDTO(savedCategorie);
    }

    @Override
    public CategorieResponseDTO mettreAJourCategorie(String id, CategorieRequestDTO dto) {
        logger.info("Mise à jour de la catégorie ID: {}", id);

        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'ID: " + id));

        // Vérifier si le nouveau nom existe déjà (sauf si c'est le même)
        if (dto.nom() != null && !dto.nom().equals(categorie.getNom())
                && categorieRepository.existsByNom(dto.nom())) {
            throw new IllegalArgumentException("Une catégorie avec le nom '" + dto.nom() + "' existe déjà");
        }

        categorieMapper.updateEntityFromDTO(categorie, dto);
        Categorie updatedCategorie = categorieRepository.save(categorie);

        logger.info("Catégorie mise à jour: {}", updatedCategorie.getIdCategorie());
        return categorieMapper.toResponseDTO(updatedCategorie);
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieResponseDTO getCategorieById(String id) {
        logger.info("Récupération de la catégorie ID: {}", id);

        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'ID: " + id));

        return categorieMapper.toResponseDTO(categorie);
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieResponseDTO getCategorieByNom(String nom) {
        logger.info("Récupération de la catégorie par nom: {}", nom);

        Categorie categorie = categorieRepository.findByNom(nom)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec le nom: " + nom));

        return categorieMapper.toResponseDTO(categorie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> getAllCategories() {
        logger.info("Récupération de toutes les catégories");

        return categorieRepository.findAll().stream()
                .map(categorieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> getCategoriesByFragile(Boolean fragile) {
        logger.info("Récupération des catégories par fragilité: {}", fragile);

        return categorieRepository.findByFragile(fragile).stream()
                .map(categorieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> getCategoriesByDangereux(Boolean dangereux) {
        logger.info("Récupération des catégories par dangerosité: {}", dangereux);

        return categorieRepository.findByDangereux(dangereux).stream()
                .map(categorieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> getCategoriesByTemperature(String temperatureRequise) {
        logger.info("Récupération des catégories par température: {}", temperatureRequise);

        return categorieRepository.findByTemperatureRequise(temperatureRequise).stream()
                .map(categorieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorieResponseDTO> searchCategories(String keyword) {
        logger.info("Recherche de catégories avec mot-clé: {}", keyword);

        return categorieRepository.findByNomContainingIgnoreCase(keyword).stream()
                .map(categorieMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerCategorie(String id) {
        logger.info("Suppression de la catégorie ID: {}", id);

        if (!categorieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Catégorie non trouvée avec l'ID: " + id);
        }

        categorieRepository.deleteById(id);
        logger.info("Catégorie supprimée: {}", id);
    }
}
