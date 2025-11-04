package ma.tna.microservice3.model;

/**
 * Énumération représentant les différents états de paiement d'une demande de transport
 */
public enum StatutPaiement {
    /**
     * Paiement en attente
     */
    EN_ATTENTE,

    /**
     * Paiement effectué
     */
    PAYEE,

    /**
     * Paiement remboursé
     */
    REMBOURSEE,

    /**
     * Paiement échoué
     */
    ECHEC
}

