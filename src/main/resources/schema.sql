-- ===============================================
-- Script d'initialisation de la base de données
-- Service 3 : Service Demandes de Transport
-- ===============================================

-- NOTES IMPORTANTES :
-- 1. Ce script doit être exécuté APRÈS avoir créé la base de données demandes_db
-- 2. Pour créer la base de données, exécutez d'abord (en dehors de ce script) :
--    CREATE DATABASE demandes_db;
-- 3. Puis connectez-vous à demandes_db et exécutez ce script

-- Création de l'utilisateur (optionnel - à exécuter en tant que superuser)
-- CREATE USER demandes_user WITH PASSWORD 'demandes_password';
-- GRANT ALL PRIVILEGES ON DATABASE demandes_db TO demandes_user;

-- La table sera créée automatiquement par Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- Mais vous pouvez aussi la créer manuellement avec ce script :

-- Table demandes
CREATE TABLE IF NOT EXISTS demandes (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    nature_marchandise VARCHAR(255) NOT NULL,
    date_depart TIMESTAMP NOT NULL,
    adresse_depart VARCHAR(500) NOT NULL,
    adresse_destination VARCHAR(500) NOT NULL,
    statut_validation VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE_CLIENT',
    statut_paiement VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    itineraire_associe_id BIGINT,
    groupe_id BIGINT,
    devis_estime DECIMAL(10, 2),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_demandes_client_id ON demandes(client_id);
CREATE INDEX IF NOT EXISTS idx_demandes_statut_validation ON demandes(statut_validation);
CREATE INDEX IF NOT EXISTS idx_demandes_statut_paiement ON demandes(statut_paiement);
CREATE INDEX IF NOT EXISTS idx_demandes_date_depart ON demandes(date_depart);

-- Données de test (optionnel)
INSERT INTO demandes (client_id, volume, nature_marchandise, date_depart, adresse_depart, adresse_destination, statut_validation, statut_paiement, devis_estime)
VALUES
    (1, 15.5, 'Meubles', '2024-12-15 10:00:00', '123 Rue Example, Paris', '456 Avenue Test, Lyon', 'EN_ATTENTE_CLIENT', 'EN_ATTENTE', 250.00),
    (1, 8.0, 'Électroménager', '2024-12-20 14:00:00', '789 Boulevard Demo, Marseille', '321 Rue Sample, Nice', 'VALIDEE_CLIENT', 'EN_ATTENTE', 180.00),
    (2, 25.0, 'Matériaux de construction', '2024-12-18 08:00:00', '555 Avenue Construction, Toulouse', '666 Rue Bâtiment, Bordeaux', 'EN_ATTENTE_CLIENT', 'EN_ATTENTE', 450.00)
ON CONFLICT (id) DO NOTHING;

-- Fin du script

