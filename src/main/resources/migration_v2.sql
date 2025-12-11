-- ===============================================
-- Migration Script v2 - Service Demandes Transport
-- Changes: 
--   - Rename adresse_depart -> ville_depart
--   - Rename adresse_destination -> ville_destination  
--   - Rename groupe_id -> mission_id
--   - Add poids column
-- ===============================================

-- Run this on the PostgreSQL database (demandes_db)

-- 1. Rename columns
ALTER TABLE demandes RENAME COLUMN adresse_depart TO ville_depart;
ALTER TABLE demandes RENAME COLUMN adresse_destination TO ville_destination;
ALTER TABLE demandes RENAME COLUMN groupe_id TO mission_id;

-- 2. Add poids column (if not exists)
ALTER TABLE demandes ADD COLUMN IF NOT EXISTS poids DOUBLE PRECISION;

-- 3. Optional: Update column types if needed (from VARCHAR(500) to VARCHAR(255) for city names)
-- ALTER TABLE demandes ALTER COLUMN ville_depart TYPE VARCHAR(255);
-- ALTER TABLE demandes ALTER COLUMN ville_destination TYPE VARCHAR(255);

-- Verify the changes
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'demandes';
