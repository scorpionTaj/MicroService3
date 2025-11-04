package ma.tna.microservice3.model;

/**
 * Énumération représentant les différents états de validation d'une demande de transport
 */
public enum StatutValidation {
    /**
     * Demande créée, en attente de validation par le client
     */
    EN_ATTENTE_CLIENT,

    /**
     * Demande validée par le client
     */
    VALIDEE_CLIENT,

    /**
     * Demande validée par le prestataire
     */
    VALIDEE_PRESTATAIRE,

    /**
     * Demande terminée
     */
    TERMINEE,

    /**
     * Demande annulée
     */
    ANNULEE
}

