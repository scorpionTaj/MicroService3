package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.CategorieResponseDTO;
import ma.tna.microservice3.dto.DemandeAssociationDTO;
import ma.tna.microservice3.dto.DemandeRequestDTO;
import ma.tna.microservice3.dto.DemandeResponseDTO;
import ma.tna.microservice3.dto.ItineraireResponseDTO;
import ma.tna.microservice3.dto.TarifResponseDTO;
import ma.tna.microservice3.exception.ResourceNotFoundException;
import ma.tna.microservice3.exception.UnauthorizedException;
import ma.tna.microservice3.mapper.DemandeMapper;
import ma.tna.microservice3.model.Categorie;
import ma.tna.microservice3.model.Demande;
import ma.tna.microservice3.model.StatutValidation;
import ma.tna.microservice3.repository.CategorieRepository;
import ma.tna.microservice3.repository.DemandeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation du service de gestion des demandes de transport
 */
@Service
@Transactional
public class DemandeServiceImpl implements DemandeService {

    private static final Logger logger = LoggerFactory.getLogger(DemandeServiceImpl.class);

    private final DemandeRepository demandeRepository;
    private final CategorieRepository categorieRepository;
    private final DemandeMapper demandeMapper;
    private final WebClient webClient;

    @Value("${service.url.itineraires}")
    private String itinerairesServiceUrl;

    @Value("${service.url.tarification}")
    private String tarificationServiceUrl;

    @Value("${service.url.matching}")
    private String matchingServiceUrl;

    public DemandeServiceImpl(
            DemandeRepository demandeRepository,
            CategorieRepository categorieRepository,
            DemandeMapper demandeMapper,
            WebClient webClient
    ) {
        this.demandeRepository = demandeRepository;
        this.categorieRepository = categorieRepository;
        this.demandeMapper = demandeMapper;
        this.webClient = webClient;
    }

    @Override
    public DemandeResponseDTO creerDemande(DemandeRequestDTO dto, Long userId) {
        logger.info("Création d'une demande pour le client ID: {}", userId);

        // 1. Créer et sauvegarder la demande initiale
        Demande demande = demandeMapper.toEntity(dto, userId);

        // 2. Associer la catégorie si fournie
        if (dto.categorieId() != null && !dto.categorieId().isBlank()) {
            Categorie categorie = categorieRepository.findById(dto.categorieId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Catégorie non trouvée avec l'ID: " + dto.categorieId()));
            demande.setCategorie(categorie);
            logger.info("Catégorie '{}' associée à la demande", categorie.getNom());
        }

        demande = demandeRepository.save(demande);

        final Long demandeId = demande.getId();

        try {
            // 2. Appel au Service Itinéraires (MS4) pour calculer l'itinéraire
            ItineraireResponseDTO itineraire = appelServiceItineraires(
                    demande.getVilleDepart(),
                    demande.getVilleDestination(),
                    userId,
                    demande.getVolume(),
                    demande.getNatureMarchandise(),
                    demande.getDateDepart()
            );

            if (itineraire != null && itineraire.routeId() != null) {
                demande.setItineraireAssocieId(itineraire.routeId());
                demande.setDistanceKm(itineraire.totalDistanceKm());
                demande.setDureeEstimeeMin(itineraire.totalDurationMin());
                logger.info("Itinéraire associé ID: {} (distance: {} km, durée: {} min) pour la demande ID: {}", 
                        itineraire.routeId(), itineraire.totalDistanceKm(), itineraire.totalDurationMin(), demandeId);
            }

            // 3. Appel au Service Tarification pour obtenir un devis
            // Note: Le service tarification peut utiliser la distance pour calculer le prix
            TarifResponseDTO tarif = appelServiceTarification(
                    demande.getVolume(),
                    demande.getDistanceKm()
            );

            if (tarif != null) {
                demande.setDevisEstime(tarif.montant());
                logger.info("Devis estimé: {} pour la demande ID: {}", tarif.montant(), demandeId);
            }

            // 4. Sauvegarder la demande avec les informations mises à jour
            demande = demandeRepository.save(demande);

        } catch (Exception e) {
            logger.error("Erreur lors des appels inter-services pour la demande ID: {}", demandeId, e);
            // La demande est quand même créée, mais sans itinéraire ni devis
        }

        return demandeMapper.toResponseDTO(demande);
    }

    @Override
    public DemandeResponseDTO validerDemandeClient(Long demandeId, Long userId) {
        logger.info("Validation de la demande ID: {} par le client ID: {}", demandeId, userId);

        // 1. Récupérer la demande et vérifier l'autorisation
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        if (!demande.getClientId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à valider cette demande");
        }

        // 2. Mettre à jour le statut de validation
        demande.setStatutValidation(StatutValidation.VALIDEE_CLIENT);
        demande = demandeRepository.save(demande);

        // 3. Appel asynchrone au Service Matching
        try {
            appelServiceMatching(demandeId);
            logger.info("Appel au service Matching effectué pour la demande ID: {}", demandeId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'appel au service Matching pour la demande ID: {}", demandeId, e);
        }

        return demandeMapper.toResponseDTO(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeResponseDTO getDemandeById(Long demandeId, Long userId) {
        logger.info("Récupération de la demande ID: {} par l'utilisateur ID: {}", demandeId, userId);

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        // Vérifier que l'utilisateur est bien le propriétaire de la demande
        if (!demande.getClientId().equals(userId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à consulter cette demande");
        }

        return demandeMapper.toResponseDTO(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponseDTO> getDemandesByClient(Long userId) {
        logger.info("Récupération des demandes pour le client ID: {}", userId);

        List<Demande> demandes = demandeRepository.findByClientId(userId);

        return demandes.stream()
                .map(demandeMapper::toResponseDTO)
                .toList();
    }

    // ============ Méthodes privées pour les appels inter-services ============

    /**
     * Appel au Service Itinéraires (MS4) pour obtenir un itinéraire
     * Utilise l'endpoint /routes/address du service Itinéraires
     * Endpoint: POST /api/routes/address
     */
    private ItineraireResponseDTO appelServiceItineraires(String villeDepart, String villeDestination, Long userId, Double volume, String natureMarchandise, java.time.LocalDateTime dateDepart) {
        try {
            logger.info("Appel au service Itinéraires: {} -> {}", villeDepart, villeDestination);

            // Construire le body selon l'API MS4 /routes/address
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("originAddress", villeDepart);
            requestBody.put("destinationAddress", villeDestination);
            requestBody.put("userId", userId != null ? userId.toString() : "anonymous");
            requestBody.put("includeReturn", false);

            logger.debug("Request body pour MS4: {}", requestBody);

            return webClient.post()
                    .uri(itinerairesServiceUrl + "/address")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(ItineraireResponseDTO.class)
                    .doOnNext(response -> logger.info("Réponse MS4: routeId={}, distance={} km, durée={} min", 
                            response.routeId(), response.totalDistanceKm(), response.totalDurationMin()))
                    .onErrorResume(e -> {
                        logger.error("Erreur lors de l'appel au service Itinéraires: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Itinéraires", e);
            return null;
        }
    }

    /**
     * Surcharge pour compatibilité - appel simplifié
     */
    private ItineraireResponseDTO appelServiceItineraires(String villeDepart, String villeDestination) {
        return appelServiceItineraires(villeDepart, villeDestination, null, null, null, null);
    }

    @Override
    public DemandeResponseDTO associerDemande(Long demandeId, DemandeAssociationDTO associationDTO) {
        logger.info("Association de la demande ID: {} avec mission ID: {} et itinéraire ID: {}",
                demandeId, associationDTO.missionId(), associationDTO.itineraireId());

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        // Mettre à jour la mission
        demande.setMissionId(associationDTO.missionId());

        // Mettre à jour l'itinéraire si fourni
        if (associationDTO.itineraireId() != null && !associationDTO.itineraireId().isBlank()) {
            demande.setItineraireAssocieId(associationDTO.itineraireId());
        }

        // Mettre à jour la distance si fournie
        if (associationDTO.distanceKm() != null) {
            demande.setDistanceKm(associationDTO.distanceKm());
        }

        // Mettre à jour la durée estimée si fournie
        if (associationDTO.dureeEstimeeMin() != null) {
            demande.setDureeEstimeeMin(associationDTO.dureeEstimeeMin());
        }

        demande = demandeRepository.save(demande);
        logger.info("Demande ID: {} mise à jour avec succès", demandeId);

        return demandeMapper.toResponseDTO(demande);
    }

    /**
     * Appel au Service Tarification pour obtenir un devis
     * @param volume Volume de la marchandise en m³
     * @param distanceKm Distance en kilomètres (depuis le service itinéraires)
     */
    private TarifResponseDTO appelServiceTarification(Double volume, Double distanceKm) {
        try {
            logger.debug("Appel au service Tarification: volume={}, distanceKm={}", volume, distanceKm);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("volume", volume);
            if (distanceKm != null) {
                requestBody.put("distanceKm", distanceKm);
            }

            return webClient.post()
                    .uri(tarificationServiceUrl + "/calculer")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(TarifResponseDTO.class)
                    .onErrorResume(e -> {
                        logger.error("Erreur lors de l'appel au service Tarification", e);
                        // Retourner un devis par défaut en cas d'erreur (basé sur volume et distance)
                        BigDecimal defaultDevis = calculateDefaultDevis(volume, distanceKm);
                        return Mono.just(new TarifResponseDTO(
                                defaultDevis,
                                "Devis estimé (service indisponible)",
                                null
                        ));
                    })
                    .block();

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Tarification", e);
            BigDecimal defaultDevis = calculateDefaultDevis(volume, distanceKm);
            return new TarifResponseDTO(defaultDevis, "Devis estimé (erreur)", null);
        }
    }

    /**
     * Calcul d'un devis par défaut basé sur le volume et la distance
     */
    private BigDecimal calculateDefaultDevis(Double volume, Double distanceKm) {
        double basePrice = 50.0; // Prix de base
        double volumeRate = volume != null ? volume * 5.0 : 0; // 5 MAD par m³
        double distanceRate = distanceKm != null ? distanceKm * 2.0 : 0; // 2 MAD par km
        return BigDecimal.valueOf(basePrice + volumeRate + distanceRate);
    }

    /**
     * Appel au Service Matching pour trouver un transporteur
     */
    private void appelServiceMatching(Long demandeId) {
        try {
            logger.debug("Appel au service Matching pour la demande ID: {}", demandeId);

            webClient.post()
                    .uri(matchingServiceUrl + "/rechercher")
                    .bodyValue(Map.of("demandeId", demandeId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> logger.info("Service Matching appelé avec succès: {}", response),
                            error -> logger.error("Erreur lors de l'appel au service Matching", error)
                    );

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Matching", e);
        }
    }
}

