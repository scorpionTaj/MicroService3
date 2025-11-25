package ma.tna.microservice3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.service.DemandeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le DemandeController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemandeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DemandeService demandeService;

    private DemandeRequestDTO requestDTO;
    private DemandeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new DemandeRequestDTO(
                15.5,
                "Meubles",
                LocalDateTime.now().plusDays(10),
                "123 Rue Example, Paris",
                "456 Avenue Test, Lyon"
        );

        responseDTO = new DemandeResponseDTO(
                1L,
                1L,
                15.5,
                "Meubles",
                LocalDateTime.now().plusDays(10),
                "123 Rue Example, Paris",
                "456 Avenue Test, Lyon",
                "EN_ATTENTE_CLIENT",
                "EN_ATTENTE",
                BigDecimal.valueOf(250.00),
                1L,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testCreerDemande_Success() throws Exception {
        when(demandeService.creerDemande(any(DemandeRequestDTO.class), anyLong()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/v1/demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.volume").value(15.5))
                .andExpect(jsonPath("$.statutValidation").value("EN_ATTENTE_CLIENT"));
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testValiderDemande_Success() throws Exception {
        DemandeResponseDTO validatedDTO = new DemandeResponseDTO(
                1L, 1L, 15.5, "Meubles",
                LocalDateTime.now().plusDays(10),
                "123 Rue Example, Paris",
                "456 Avenue Test, Lyon",
                "VALIDEE_CLIENT",
                "EN_ATTENTE",
                BigDecimal.valueOf(250.00),
                1L, null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(demandeService.validerDemandeClient(anyLong(), anyLong()))
                .thenReturn(validatedDTO);

        mockMvc.perform(put("/api/v1/demandes/1/validation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statutValidation").value("VALIDEE_CLIENT"));
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testGetMesDemandesClient_Success() throws Exception {
        List<DemandeResponseDTO> demandes = Arrays.asList(responseDTO);

        when(demandeService.getDemandesByClient(anyLong()))
                .thenReturn(demandes);

        mockMvc.perform(get("/api/v1/demandes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].clientId").value(1));
    }

    @Test
    @WithMockUser(username = "1", roles = "CLIENT")
    void testGetDemandeById_Success() throws Exception {
        when(demandeService.getDemandeById(anyLong(), anyLong()))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/demandes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clientId").value(1));
    }

    @Test
    void testCreerDemande_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/demandes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }
}

