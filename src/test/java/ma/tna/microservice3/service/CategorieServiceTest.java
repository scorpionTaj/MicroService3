package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.exception.ResourceNotFoundException;
import ma.tna.microservice3.mapper.CategorieMapper;
import ma.tna.microservice3.model.Categorie;
import ma.tna.microservice3.repository.CategorieRepository;
import ma.tna.microservice3.service.impl.CategorieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategorieServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class CategorieServiceTest {

    @Mock
    private CategorieRepository categorieRepository;

    @Mock
    private CategorieMapper categorieMapper;

    @InjectMocks
    private CategorieServiceImpl categorieService;

    private Categorie categorie;
    private CategorieRequestDTO requestDTO;
    private CategorieResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        categorie = Categorie.builder()
                .idCategorie("cat-001-test")
                .nom("Meubles")
                .description("Meubles et mobilier domestique")
                .densiteMoyenne(250.0)
                .fragile(true)
                .dangereux(false)
                .temperatureRequise("ambiante")
                .restrictions("Protéger les angles")
                .dateCreation(now)
                .dateModification(now)
                .build();

        requestDTO = new CategorieRequestDTO(
                "Meubles",
                "Meubles et mobilier domestique",
                250.0,
                true,
                false,
                "ambiante",
                "Protéger les angles"
        );

        responseDTO = new CategorieResponseDTO(
                "cat-001-test",
                "Meubles",
                "Meubles et mobilier domestique",
                250.0,
                true,
                false,
                "ambiante",
                "Protéger les angles",
                now,
                now
        );
    }

    // ==================== Tests de Création ====================

    @Test
    @DisplayName("Créer une catégorie avec succès")
    void creerCategorie_ShouldReturnCreatedCategorie() {
        when(categorieRepository.existsByNom("Meubles")).thenReturn(false);
        when(categorieMapper.toEntity(requestDTO)).thenReturn(categorie);
        when(categorieRepository.save(categorie)).thenReturn(categorie);
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        CategorieResponseDTO result = categorieService.creerCategorie(requestDTO);

        assertNotNull(result);
        assertEquals("cat-001-test", result.idCategorie());
        assertEquals("Meubles", result.nom());
        assertTrue(result.fragile());
        verify(categorieRepository).save(categorie);
    }

    @Test
    @DisplayName("Créer une catégorie avec nom existant devrait échouer")
    void creerCategorie_WithExistingName_ShouldThrowException() {
        when(categorieRepository.existsByNom("Meubles")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            categorieService.creerCategorie(requestDTO);
        });

        verify(categorieRepository, never()).save(any());
    }

    // ==================== Tests de Mise à jour ====================

    @Test
    @DisplayName("Mettre à jour une catégorie avec succès")
    void mettreAJourCategorie_ShouldReturnUpdatedCategorie() {
        when(categorieRepository.findById("cat-001-test")).thenReturn(Optional.of(categorie));
        when(categorieRepository.existsByNom("Meubles")).thenReturn(false);
        when(categorieRepository.save(categorie)).thenReturn(categorie);
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        CategorieResponseDTO result = categorieService.mettreAJourCategorie("cat-001-test", requestDTO);

        assertNotNull(result);
        assertEquals("cat-001-test", result.idCategorie());
        verify(categorieMapper).updateEntityFromDTO(categorie, requestDTO);
    }

    @Test
    @DisplayName("Mettre à jour une catégorie inexistante devrait échouer")
    void mettreAJourCategorie_NotFound_ShouldThrowException() {
        when(categorieRepository.findById("cat-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categorieService.mettreAJourCategorie("cat-999", requestDTO);
        });
    }

    // ==================== Tests de Récupération ====================

    @Test
    @DisplayName("Récupérer une catégorie par ID")
    void getCategorieById_ShouldReturnCategorie() {
        when(categorieRepository.findById("cat-001-test")).thenReturn(Optional.of(categorie));
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        CategorieResponseDTO result = categorieService.getCategorieById("cat-001-test");

        assertNotNull(result);
        assertEquals("cat-001-test", result.idCategorie());
    }

    @Test
    @DisplayName("Récupérer une catégorie inexistante devrait échouer")
    void getCategorieById_NotFound_ShouldThrowException() {
        when(categorieRepository.findById("cat-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            categorieService.getCategorieById("cat-999");
        });
    }

    @Test
    @DisplayName("Récupérer une catégorie par nom")
    void getCategorieByNom_ShouldReturnCategorie() {
        when(categorieRepository.findByNom("Meubles")).thenReturn(Optional.of(categorie));
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        CategorieResponseDTO result = categorieService.getCategorieByNom("Meubles");

        assertNotNull(result);
        assertEquals("Meubles", result.nom());
    }

    @Test
    @DisplayName("Récupérer toutes les catégories")
    void getAllCategories_ShouldReturnAllCategories() {
        List<Categorie> categories = Arrays.asList(categorie);
        when(categorieRepository.findAll()).thenReturn(categories);
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        List<CategorieResponseDTO> result = categorieService.getAllCategories();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== Tests de Filtrage ====================

    @Test
    @DisplayName("Filtrer les catégories par fragilité")
    void getCategoriesByFragile_ShouldReturnFilteredCategories() {
        List<Categorie> categories = Arrays.asList(categorie);
        when(categorieRepository.findByFragile(true)).thenReturn(categories);
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        List<CategorieResponseDTO> result = categorieService.getCategoriesByFragile(true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).fragile());
    }

    @Test
    @DisplayName("Filtrer les catégories par dangerosité")
    void getCategoriesByDangereux_ShouldReturnFilteredCategories() {
        when(categorieRepository.findByDangereux(false)).thenReturn(Arrays.asList(categorie));
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        List<CategorieResponseDTO> result = categorieService.getCategoriesByDangereux(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).dangereux());
    }

    @Test
    @DisplayName("Filtrer les catégories par température")
    void getCategoriesByTemperature_ShouldReturnFilteredCategories() {
        when(categorieRepository.findByTemperatureRequise("ambiante")).thenReturn(Arrays.asList(categorie));
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        List<CategorieResponseDTO> result = categorieService.getCategoriesByTemperature("ambiante");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ambiante", result.get(0).temperatureRequise());
    }

    @Test
    @DisplayName("Rechercher les catégories par mot-clé")
    void searchCategories_ShouldReturnMatchingCategories() {
        when(categorieRepository.findByNomContainingIgnoreCase("Meub")).thenReturn(Arrays.asList(categorie));
        when(categorieMapper.toResponseDTO(categorie)).thenReturn(responseDTO);

        List<CategorieResponseDTO> result = categorieService.searchCategories("Meub");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== Tests de Suppression ====================

    @Test
    @DisplayName("Supprimer une catégorie avec succès")
    void supprimerCategorie_ShouldDeleteCategorie() {
        when(categorieRepository.existsById("cat-001-test")).thenReturn(true);

        categorieService.supprimerCategorie("cat-001-test");

        verify(categorieRepository).deleteById("cat-001-test");
    }

    @Test
    @DisplayName("Supprimer une catégorie inexistante devrait échouer")
    void supprimerCategorie_NotFound_ShouldThrowException() {
        when(categorieRepository.existsById("cat-999")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            categorieService.supprimerCategorie("cat-999");
        });

        verify(categorieRepository, never()).deleteById(any());
    }
}
