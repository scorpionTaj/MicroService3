package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.ClientInfoDTO;
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

    @Value("${service.url.utilisateurs}")
    private String utilisateursServiceUrl;

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
            // L'itinéraire sera associé par un autre microservice via l'endpoint /association
            // itineraireAssocieId reste null à la création

            // Appel au Service Tarification pour obtenir un devis
            TarifResponseDTO tarif = appelServiceTarification(
                    demande.getVolume(),
                    null
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
    public DemandeResponseDTO getDemandeById(Long demandeId, Long userId, String role) {
        logger.info("Récupération de la demande ID: {} par l'utilisateur ID: {} avec rôle: {}", demandeId, userId, role);

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        // ADMIN peut voir toutes les demandes
        if ("ADMIN".equalsIgnoreCase(role)) {
            return demandeMapper.toResponseDTO(demande);
        }

        // PRESTATAIRE peut voir les demandes de ses missions
        if ("PRESTATAIRE".equalsIgnoreCase(role)) {
            // Pour l'instant, on autorise les prestataires à voir toutes les demandes validées
            // Plus tard, on pourra filtrer par mission assignée au prestataire
            if (demande.getStatutValidation() != StatutValidation.EN_ATTENTE_CLIENT) {
                return demandeMapper.toResponseDTO(demande);
            }
        }

        // CLIENT ne peut voir que SES demandes
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

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponseDTO> getAllDemandes() {
        logger.info("Récupération de TOUTES les demandes (Admin)");

        List<Demande> demandes = demandeRepository.findAll();

        return demandes.stream()
                .map(demandeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponseDTO> getDemandesByStatut(String statut) {
        logger.info("Récupération des demandes par statut: {}", statut);

        StatutValidation statutValidation = StatutValidation.valueOf(statut.toUpperCase());
        List<Demande> demandes = demandeRepository.findByStatutValidation(statutValidation);

        return demandes.stream()
                .map(demandeMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResponseDTO> getDemandesByMission(Long missionId) {
        logger.info("Récupération des demandes pour la mission ID: {}", missionId);

        List<Demande> demandes = demandeRepository.findByMissionId(missionId);

        return demandes.stream()
                .map(demandeMapper::toResponseDTO)
                .toList();
    }

    @Override
    public DemandeResponseDTO updateStatut(Long demandeId, String nouveauStatut) {
        logger.info("Mise à jour du statut de la demande ID: {} vers: {}", demandeId, nouveauStatut);

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        StatutValidation statutValidation = StatutValidation.valueOf(nouveauStatut.toUpperCase());
        demande.setStatutValidation(statutValidation);
        demande = demandeRepository.save(demande);

        logger.info("Statut de la demande ID: {} mis à jour vers: {}", demandeId, nouveauStatut);
        return demandeMapper.toResponseDTO(demande);
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
                demandeId, associationDTO.missionId(), associationDTO.itineraireAssocieId());

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        // Mettre à jour la mission
        demande.setMissionId(associationDTO.missionId());

        // Mettre à jour l'itinéraire si fourni
        if (associationDTO.itineraireAssocieId() != null && !associationDTO.itineraireAssocieId().isBlank()) {
            demande.setItineraireAssocieId(associationDTO.itineraireAssocieId());
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

    @Override
    @Transactional(readOnly = true)
    public ClientInfoDTO getClientInfoByDemande(Long demandeId, Long userId, String role, String authToken) {
        logger.info("Récupération des infos client pour la demande ID: {} par l'utilisateur ID: {} avec rôle: {}", 
                demandeId, userId, role);

        // 1. Récupérer la demande et vérifier les droits
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        // Vérifier les droits d'accès
        if (!"ADMIN".equalsIgnoreCase(role) && !"PRESTATAIRE".equalsIgnoreCase(role)) {
            // CLIENT ne peut voir que ses propres demandes
            if (!demande.getClientId().equals(userId)) {
                throw new UnauthorizedException("Vous n'êtes pas autorisé à consulter les informations de ce client");
            }
        }

        Long clientId = demande.getClientId();

        // 2. Appeler le Service Utilisateurs pour récupérer les infos
        try {
            logger.info("Appel au service Utilisateurs pour le client ID: {}", clientId);

            // L'URL du service utilisateurs est: http://172.30.80.11:31019/account
            // L'endpoint est: GET /users/{id}/
            String url = utilisateursServiceUrl.replace("/account", "") + "/account/users/" + clientId + "/";

            ClientInfoDTO clientInfo = webClient.get()
                    .uri(url)
                    .header("Authorization", authToken)
                    .retrieve()
                    .bodyToMono(ClientInfoDTO.class)
                    .doOnNext(response -> logger.info("Infos client récupérées: {}", response.email()))
                    .onErrorResume(e -> {
                        logger.error("Erreur lors de l'appel au service Utilisateurs: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (clientInfo == null) {
                throw new ResourceNotFoundException("Impossible de récupérer les informations du client ID: " + clientId);
            }

            return clientInfo;

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Utilisateurs", e);
            throw new ResourceNotFoundException("Erreur lors de la récupération des informations du client: " + e.getMessage());
        }
    }
}

