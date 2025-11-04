package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.exception.ResourceNotFoundException;
import ma.tna.microservice3.exception.UnauthorizedException;
import ma.tna.microservice3.mapper.DemandeMapper;
import ma.tna.microservice3.model.Demande;
import ma.tna.microservice3.model.StatutPaiement;
import ma.tna.microservice3.model.StatutValidation;
import ma.tna.microservice3.repository.DemandeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour DemandeServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class DemandeServiceImplTest {

    @Mock
    private DemandeRepository demandeRepository;

    @Mock
    private DemandeMapper demandeMapper;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private DemandeServiceImpl demandeService;

    private Demande demande;
    private DemandeRequestDTO requestDTO;
    private DemandeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        demande = Demande.builder()
                .id(1L)
                .clientId(1L)
                .volume(15.5)
                .natureMarchandise("Meubles")
                .dateDepart(LocalDateTime.now().plusDays(10))
                .adresseDepart("123 Rue Example, Paris")
                .adresseDestination("456 Avenue Test, Lyon")
                .statutValidation(StatutValidation.EN_ATTENTE_CLIENT)
                .statutPaiement(StatutPaiement.EN_ATTENTE)
                .devisEstime(BigDecimal.valueOf(250.00))
                .build();

        requestDTO = new DemandeRequestDTO(
                15.5,
                "Meubles",
                LocalDateTime.now().plusDays(10),
                "123 Rue Example, Paris",
                "456 Avenue Test, Lyon"
        );

        responseDTO = new DemandeResponseDTO(
                1L, 1L, 15.5, "Meubles",
                LocalDateTime.now().plusDays(10),
                "123 Rue Example, Paris",
                "456 Avenue Test, Lyon",
                "EN_ATTENTE_CLIENT",
                "EN_ATTENTE",
                BigDecimal.valueOf(250.00),
                1L, null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void testCreerDemande_Success() {
        // Arrange
        when(demandeMapper.toEntity(any(DemandeRequestDTO.class), anyLong()))
                .thenReturn(demande);
        when(demandeRepository.save(any(Demande.class)))
                .thenReturn(demande);
        when(demandeMapper.toResponseDTO(any(Demande.class)))
                .thenReturn(responseDTO);

        // Act
        DemandeResponseDTO result = demandeService.creerDemande(requestDTO, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.clientId()).isEqualTo(1L);
        assertThat(result.statutValidation()).isEqualTo("EN_ATTENTE_CLIENT");

        verify(demandeRepository, atLeastOnce()).save(any(Demande.class));
        verify(demandeMapper).toResponseDTO(any(Demande.class));
    }

    @Test
    void testGetDemandeById_Success() {
        // Arrange
        when(demandeRepository.findById(1L))
                .thenReturn(Optional.of(demande));
        when(demandeMapper.toResponseDTO(any(Demande.class)))
                .thenReturn(responseDTO);

        // Act
        DemandeResponseDTO result = demandeService.getDemandeById(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);

        verify(demandeRepository).findById(1L);
    }

    @Test
    void testGetDemandeById_NotFound() {
        // Arrange
        when(demandeRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> demandeService.getDemandeById(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Demande non trouvée");
    }

    @Test
    void testGetDemandeById_Unauthorized() {
        // Arrange
        when(demandeRepository.findById(1L))
                .thenReturn(Optional.of(demande));

        // Act & Assert
        assertThatThrownBy(() -> demandeService.getDemandeById(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("pas autorisé");
    }

    @Test
    void testGetDemandesByClient_Success() {
        // Arrange
        List<Demande> demandes = Arrays.asList(demande);
        when(demandeRepository.findByClientId(1L))
                .thenReturn(demandes);
        when(demandeMapper.toResponseDTO(any(Demande.class)))
                .thenReturn(responseDTO);

        // Act
        List<DemandeResponseDTO> result = demandeService.getDemandesByClient(1L);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientId()).isEqualTo(1L);

        verify(demandeRepository).findByClientId(1L);
    }

    @Test
    void testValiderDemandeClient_Success() {
        // Arrange
        when(demandeRepository.findById(1L))
                .thenReturn(Optional.of(demande));
        when(demandeRepository.save(any(Demande.class)))
                .thenReturn(demande);
        when(demandeMapper.toResponseDTO(any(Demande.class)))
                .thenReturn(responseDTO);

        // Act
        DemandeResponseDTO result = demandeService.validerDemandeClient(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        verify(demandeRepository).findById(1L);
        verify(demandeRepository).save(any(Demande.class));
    }
}

