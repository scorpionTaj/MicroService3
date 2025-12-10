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

-- Table categories (doit être créée avant demandes car demandes y fait référence)
CREATE TABLE IF NOT EXISTS categories (
    id_categorie VARCHAR(36) PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    densite_moyenne DOUBLE PRECISION,
    fragile BOOLEAN NOT NULL DEFAULT FALSE,
    dangereux BOOLEAN NOT NULL DEFAULT FALSE,
    temperature_requise VARCHAR(50) DEFAULT 'ambiante',
    restrictions VARCHAR(500),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table demandes
CREATE TABLE IF NOT EXISTS demandes (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    volume DOUBLE PRECISION NOT NULL,
    poids DOUBLE PRECISION,
    nature_marchandise VARCHAR(255) NOT NULL,
    date_depart TIMESTAMP NOT NULL,
    ville_depart VARCHAR(255) NOT NULL,
    ville_destination VARCHAR(255) NOT NULL,
    statut_validation VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE_CLIENT',
    itineraire_associe_id VARCHAR(100),
    distance_km DOUBLE PRECISION,
    duree_estimee_min INTEGER,
    mission_id BIGINT,
    categorie_id VARCHAR(36) REFERENCES categories(id_categorie),
    devis_estime DECIMAL(10, 2),
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_demandes_client_id ON demandes(client_id);
CREATE INDEX IF NOT EXISTS idx_demandes_statut_validation ON demandes(statut_validation);
CREATE INDEX IF NOT EXISTS idx_demandes_date_depart ON demandes(date_depart);
CREATE INDEX IF NOT EXISTS idx_demandes_categorie_id ON demandes(categorie_id);

-- Index pour la table categories
CREATE INDEX IF NOT EXISTS idx_categories_nom ON categories(nom);
CREATE INDEX IF NOT EXISTS idx_categories_fragile ON categories(fragile);
CREATE INDEX IF NOT EXISTS idx_categories_dangereux ON categories(dangereux);
CREATE INDEX IF NOT EXISTS idx_categories_temperature ON categories(temperature_requise);

-- Données de test pour les catégories
INSERT INTO categories (id_categorie, nom, description, densite_moyenne, fragile, dangereux, temperature_requise, restrictions)
VALUES
    ('cat-001-meubles', 'Meubles', 'Meubles et mobilier domestique', 250.0, true, false, 'ambiante', 'Protéger les angles et surfaces fragiles'),
    ('cat-002-electro', 'Électroménager', 'Appareils électroménagers', 450.0, true, false, 'ambiante', 'Ne pas renverser, manipuler avec précaution'),
    ('cat-003-aliment', 'Produits Alimentaires', 'Denrées alimentaires périssables', 850.0, false, false, 'refrigere', 'Respecter la chaîne du froid'),
    ('cat-004-surgele', 'Produits Surgelés', 'Aliments surgelés et congelés', 900.0, false, false, 'congele', 'Maintenir à -18°C minimum'),
    ('cat-005-constr', 'Matériaux de Construction', 'Matériaux pour le bâtiment', 1500.0, false, false, 'ambiante', 'Protéger de l''humidité'),
    ('cat-006-chimiq', 'Produits Chimiques', 'Produits chimiques industriels', 1200.0, false, true, 'ambiante', 'Transport ADR requis, ventilation obligatoire'),
    ('cat-007-pharma', 'Produits Pharmaceutiques', 'Médicaments et produits de santé', 300.0, true, false, 'refrigere', 'Conserver entre 2°C et 8°C'),
    ('cat-008-texti', 'Textiles', 'Vêtements et tissus', 150.0, false, false, 'ambiante', 'Protéger de l''humidité et des odeurs')
ON CONFLICT (id_categorie) DO NOTHING;

-- Données de test (optionnel)
INSERT INTO demandes (client_id, volume, poids, nature_marchandise, date_depart, ville_depart, ville_destination, statut_validation, categorie_id, devis_estime)
VALUES
    (1, 15.5, 250.0, 'Meubles', '2024-12-15 10:00:00', 'Paris', 'Lyon', 'EN_ATTENTE_CLIENT', 'cat-001-meubles', 250.00),
    (1, 8.0, 120.0, 'Électroménager', '2024-12-20 14:00:00', 'Marseille', 'Nice', 'VALIDEE_CLIENT', 'cat-002-electro', 180.00),
    (2, 25.0, 500.0, 'Matériaux de construction', '2024-12-18 08:00:00', 'Toulouse', 'Bordeaux', 'EN_ATTENTE_CLIENT', 'cat-005-constr', 450.00)
ON CONFLICT (id) DO NOTHING;

-- Fin du script

