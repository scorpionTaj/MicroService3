package ma.tna.microservice3.mapper;

import ma.tna.microservice3.dto.CategorieRequestDTO;
import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.model.Categorie;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre entités Categorie et DTOs
 */
@Component
public class CategorieMapper {

    /**
     * Convertit un CategorieRequestDTO en entité Categorie
     */
    public Categorie toEntity(CategorieRequestDTO dto) {
        return Categorie.builder()
                .nom(dto.nom())
                .description(dto.description())
                .densiteMoyenne(dto.densiteMoyenne())
                .fragile(dto.fragile() != null ? dto.fragile() : false)
                .dangereux(dto.dangereux() != null ? dto.dangereux() : false)
                .temperatureRequise(dto.temperatureRequise() != null ? dto.temperatureRequise() : "ambiante")
                .restrictions(dto.restrictions())
                .build();
    }

    /**
     * Convertit une entité Categorie en CategorieResponseDTO
     */
    public CategorieResponseDTO toResponseDTO(Categorie categorie) {
        return new CategorieResponseDTO(
                categorie.getIdCategorie(),
                categorie.getNom(),
                categorie.getDescription(),
                categorie.getDensiteMoyenne(),
                categorie.getFragile(),
                categorie.getDangereux(),
                categorie.getTemperatureRequise(),
                categorie.getRestrictions(),
                categorie.getDateCreation(),
                categorie.getDateModification()
        );
    }

    /**
     * Met à jour une entité Categorie à partir d'un DTO
     */
    public void updateEntityFromDTO(Categorie categorie, CategorieRequestDTO dto) {
        if (dto.nom() != null) {
            categorie.setNom(dto.nom());
        }
        if (dto.description() != null) {
            categorie.setDescription(dto.description());
        }
        if (dto.densiteMoyenne() != null) {
            categorie.setDensiteMoyenne(dto.densiteMoyenne());
        }
        if (dto.fragile() != null) {
            categorie.setFragile(dto.fragile());
        }
        if (dto.dangereux() != null) {
            categorie.setDangereux(dto.dangereux());
        }
        if (dto.temperatureRequise() != null) {
            categorie.setTemperatureRequise(dto.temperatureRequise());
        }
        if (dto.restrictions() != null) {
            categorie.setRestrictions(dto.restrictions());
        }
    }
}
