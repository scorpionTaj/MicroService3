package ma.tna.microservice3.service;

import ma.tna.microservice3.dto.*;
import ma.tna.microservice3.exception.ResourceNotFoundException;
import ma.tna.microservice3.exception.UnauthorizedException;
import ma.tna.microservice3.mapper.DemandeMapper;
import ma.tna.microservice3.model.Demande;
import ma.tna.microservice3.model.StatutValidation;
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
    private final DemandeMapper demandeMapper;
    private final WebClient webClient;

    @Value("${service.url.itineraires}")
    private String itinerairesServiceUrl;

    @Value("${service.url.tarification}")
    private String tarificationServiceUrl;

    @Value("${service.url.matching}")
    private String matchingServiceUrl;

    @Value("${service.url.paiements}")
    private String paiementsServiceUrl;

    public DemandeServiceImpl(
            DemandeRepository demandeRepository,
            DemandeMapper demandeMapper,
            WebClient webClient
    ) {
        this.demandeRepository = demandeRepository;
        this.demandeMapper = demandeMapper;
        this.webClient = webClient;
    }

    @Override
    public DemandeResponseDTO creerDemande(DemandeRequestDTO dto, Long userId) {
        logger.info("Création d'une demande pour le client ID: {}", userId);

        // 1. Créer et sauvegarder la demande initiale
        Demande demande = demandeMapper.toEntity(dto, userId);
        demande = demandeRepository.save(demande);

        final Long demandeId = demande.getId();

        try {
            // 2. Appel au Service Itinéraires (non bloquant)
            ItineraireResponseDTO itineraire = appelServiceItineraires(
                    demande.getAdresseDepart(),
                    demande.getAdresseDestination()
            );

            if (itineraire != null) {
                demande.setItineraireAssocieId(itineraire.id());
                logger.info("Itinéraire associé ID: {} pour la demande ID: {}", itineraire.id(), demandeId);
            }

            // 3. Appel au Service Tarification (non bloquant)
            TarifResponseDTO tarif = appelServiceTarification(
                    demande.getVolume(),
                    demande.getItineraireAssocieId()
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

        // 4. Appel asynchrone au Service Paiements pour initier le paiement
        try {
            appelServicePaiementsInitier(demandeId, demande.getDevisEstime(), userId);
            logger.info("Appel au service Paiements effectué pour la demande ID: {}", demandeId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'appel au service Paiements pour la demande ID: {}", demandeId, e);
        }

        return demandeMapper.toResponseDTO(demande);
    }

    @Override
    public void mettreAJourStatutPaiement(Long demandeId, PaiementStatusUpdateDTO dto) {
        logger.info("Mise à jour du statut de paiement pour la demande ID: {} -> {}", demandeId, dto.nouveauStatut());

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée avec l'ID: " + demandeId));

        demande.setStatutPaiement(dto.nouveauStatut());
        demandeRepository.save(demande);

        logger.info("Statut de paiement mis à jour avec succès pour la demande ID: {}", demandeId);
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
     * Appel au Service Itinéraires pour obtenir un itinéraire
     */
    private ItineraireResponseDTO appelServiceItineraires(String adresseDepart, String adresseDestination) {
        try {
            logger.debug("Appel au service Itinéraires: {} -> {}", adresseDepart, adresseDestination);

            return webClient.post()
                    .uri(itinerairesServiceUrl + "/calculer")
                    .bodyValue(Map.of(
                            "pointDepart", adresseDepart,
                            "pointArrivee", adresseDestination
                    ))
                    .retrieve()
                    .bodyToMono(ItineraireResponseDTO.class)
                    .onErrorResume(e -> {
                        logger.error("Erreur lors de l'appel au service Itinéraires", e);
                        return Mono.empty();
                    })
                    .block();

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Itinéraires", e);
            return null;
        }
    }

    /**
     * Appel au Service Tarification pour obtenir un devis
     */
    private TarifResponseDTO appelServiceTarification(Double volume, Long itineraireId) {
        try {
            logger.debug("Appel au service Tarification: volume={}, itineraireId={}", volume, itineraireId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("volume", volume);
            if (itineraireId != null) {
                requestBody.put("itineraireId", itineraireId);
            }

            return webClient.post()
                    .uri(tarificationServiceUrl + "/calculer")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(TarifResponseDTO.class)
                    .onErrorResume(e -> {
                        logger.error("Erreur lors de l'appel au service Tarification", e);
                        // Retourner un devis par défaut en cas d'erreur
                        return Mono.just(new TarifResponseDTO(
                                BigDecimal.valueOf(100.0),
                                "Devis estimé (service indisponible)",
                                itineraireId
                        ));
                    })
                    .block();

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Tarification", e);
            return new TarifResponseDTO(BigDecimal.valueOf(100.0), "Devis estimé (erreur)", itineraireId);
        }
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

    /**
     * Appel au Service Paiements pour initier un paiement
     */
    private void appelServicePaiementsInitier(Long demandeId, BigDecimal montant, Long userId) {
        try {
            logger.debug("Appel au service Paiements pour initier le paiement de la demande ID: {}", demandeId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("demandeId", demandeId);
            requestBody.put("montant", montant);
            requestBody.put("clientId", userId);

            webClient.post()
                    .uri(paiementsServiceUrl + "/initier")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> logger.info("Service Paiements appelé avec succès: {}", response),
                            error -> logger.error("Erreur lors de l'appel au service Paiements", error)
                    );

        } catch (Exception e) {
            logger.error("Exception lors de l'appel au service Paiements", e);
        }
    }
}

