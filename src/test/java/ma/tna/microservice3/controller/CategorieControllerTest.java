package ma.tna.microservice3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.service.CategorieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour CategorieController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategorieService categorieService;

    private CategorieResponseDTO categorieResponse;
    private CategorieRequestDTO categorieRequest;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Token JWT de test valide
        jwtToken = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQ0xJRU5UIiwidXNlcklkIjoxLCJzdWIiOiIxIiwiaWF0IjoxNzY0MTA4NjQ4LCJleHAiOjE3OTU2NDQ2NDh9.MsAIo8mq0sGFYTZ5XNK8oHU-fcQhZNCRWIJ_CxTtB2sau88MBHz4JiO6-DhhqHnl";

        LocalDateTime now = LocalDateTime.now();

        categorieResponse = new CategorieResponseDTO(
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

        categorieRequest = new CategorieRequestDTO(
                "Meubles",
                "Meubles et mobilier domestique",
                250.0,
                true,
                false,
                "ambiante",
                "Protéger les angles"
        );
    }

    // ==================== Tests de Lecture (Public) ====================

    @Test
    @DisplayName("GET /api/v1/categories - Lister toutes les catégories (public)")
    void getAllCategories_ShouldReturnList() throws Exception {
        List<CategorieResponseDTO> categories = Arrays.asList(categorieResponse);
        when(categorieService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCategorie").value("cat-001-test"))
                .andExpect(jsonPath("$[0].nom").value("Meubles"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} - Récupérer une catégorie par ID (public)")
    void getCategorieById_ShouldReturnCategorie() throws Exception {
        when(categorieService.getCategorieById("cat-001-test")).thenReturn(categorieResponse);

        mockMvc.perform(get("/api/v1/categories/cat-001-test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategorie").value("cat-001-test"))
                .andExpect(jsonPath("$.nom").value("Meubles"))
                .andExpect(jsonPath("$.fragile").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/categories/nom/{nom} - Rechercher par nom (public)")
    void getCategorieByNom_ShouldReturnCategorie() throws Exception {
        when(categorieService.getCategorieByNom("Meubles")).thenReturn(categorieResponse);

        mockMvc.perform(get("/api/v1/categories/nom/Meubles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Meubles"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/search - Rechercher par mot-clé (public)")
    void searchCategories_ShouldReturnMatchingCategories() throws Exception {
        List<CategorieResponseDTO> categories = Arrays.asList(categorieResponse);
        when(categorieService.searchCategories("Meub")).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories/search")
                        .param("keyword", "Meub")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Meubles"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/filter/fragile - Filtrer par fragilité (public)")
    void getCategoriesByFragile_ShouldReturnFragileCategories() throws Exception {
        List<CategorieResponseDTO> categories = Arrays.asList(categorieResponse);
        when(categorieService.getCategoriesByFragile(true)).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories/filter/fragile")
                        .param("fragile", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fragile").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/categories/filter/dangereux - Filtrer par dangerosité (public)")
    void getCategoriesByDangereux_ShouldReturnDangerousCategories() throws Exception {
        List<CategorieResponseDTO> categories = Arrays.asList();
        when(categorieService.getCategoriesByDangereux(true)).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories/filter/dangereux")
                        .param("dangereux", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/categories/filter/temperature - Filtrer par température (public)")
    void getCategoriesByTemperature_ShouldReturnMatchingCategories() throws Exception {
        List<CategorieResponseDTO> categories = Arrays.asList(categorieResponse);
        when(categorieService.getCategoriesByTemperature("ambiante")).thenReturn(categories);

        mockMvc.perform(get("/api/v1/categories/filter/temperature")
                        .param("temperature", "ambiante")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].temperatureRequise").value("ambiante"));
    }

    // ==================== Tests de Création (Authentifié) ====================

    @Test
    @DisplayName("POST /api/v1/categories - Créer une catégorie (avec JWT)")
    void creerCategorie_WithValidToken_ShouldCreateCategorie() throws Exception {
        when(categorieService.creerCategorie(any(CategorieRequestDTO.class))).thenReturn(categorieResponse);

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategorie").value("cat-001-test"))
                .andExpect(jsonPath("$.nom").value("Meubles"));
    }

    @Test
    @DisplayName("POST /api/v1/categories - Sans JWT devrait échouer")
    void creerCategorie_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/categories - Avec données invalides")
    void creerCategorie_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CategorieRequestDTO invalidRequest = new CategorieRequestDTO(
                "",  // nom vide - invalide
                "Description",
                250.0,
                false,
                false,
                "ambiante",
                null
        );

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests de Mise à jour (Authentifié) ====================

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Modifier une catégorie (avec JWT)")
    void mettreAJourCategorie_WithValidToken_ShouldUpdateCategorie() throws Exception {
        when(categorieService.mettreAJourCategorie(eq("cat-001-test"), any(CategorieRequestDTO.class)))
                .thenReturn(categorieResponse);

        mockMvc.perform(put("/api/v1/categories/cat-001-test")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategorie").value("cat-001-test"));
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} - Sans JWT devrait échouer")
    void mettreAJourCategorie_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(put("/api/v1/categories/cat-001-test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categorieRequest)))
                .andExpect(status().isForbidden());
    }

    // ==================== Tests de Suppression (Authentifié) ====================

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Supprimer une catégorie (avec JWT)")
    void supprimerCategorie_WithValidToken_ShouldDeleteCategorie() throws Exception {
        doNothing().when(categorieService).supprimerCategorie("cat-001-test");

        mockMvc.perform(delete("/api/v1/categories/cat-001-test")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Sans JWT devrait échouer")
    void supprimerCategorie_WithoutToken_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/cat-001-test"))
                .andExpect(status().isForbidden());
    }
}
