package ma.tna.microservice3.mapper;

import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.model.Demande;
import ma.tna.microservice3.model.StatutValidation;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre entités Demande et DTOs
 */
@Component
public class DemandeMapper {

    private final CategorieMapper categorieMapper;

    public DemandeMapper(CategorieMapper categorieMapper) {
        this.categorieMapper = categorieMapper;
    }

    /**
     * Convertit un DemandeRequestDTO en entité Demande
     */
    public Demande toEntity(DemandeRequestDTO dto, Long clientId) {
        return Demande.builder()
                .clientId(clientId)
                .volume(dto.volume())
                .poids(dto.poids())
                .natureMarchandise(dto.natureMarchandise())
                .dateDepart(dto.dateDepart())
                .villeDepart(dto.villeDepart())
                .villeDestination(dto.villeDestination())
                .statutValidation(StatutValidation.EN_ATTENTE_CLIENT)
                .build();
    }

    /**
     * Convertit une entité Demande en DemandeResponseDTO
     */
    public DemandeResponseDTO toResponseDTO(Demande demande) {
        CategorieResponseDTO categorieDTO = null;
        if (demande.getCategorie() != null) {
            categorieDTO = categorieMapper.toResponseDTO(demande.getCategorie());
        }

        return new DemandeResponseDTO(
                demande.getId(),
                demande.getClientId(),
                demande.getVolume(),
                demande.getPoids(),
                demande.getNatureMarchandise(),
                demande.getDateDepart(),
                demande.getVilleDepart(),
                demande.getVilleDestination(),
                demande.getStatutValidation().name(),
                demande.getDevisEstime(),
                demande.getItineraireAssocieId(),
                demande.getMissionId(),
                categorieDTO,
                demande.getDateCreation(),
                demande.getDateModification()
        );
    }
}

