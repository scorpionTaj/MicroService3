# API Demandes de Transport - Service 3

API REST Spring Boot permettant de :

- Créer des demandes de transport avec calcul automatique du devis
- Valider les demandes par les clients
- Gérer le cycle de vie des demandes (création → validation → affectation)
- Suivre l'historique des demandes par client

Base URL : `/api/v1/demandes`

> **Note importante** : Ce service s'intègre avec les Services 4 (Itinéraires), 5 (Tarification) et 8 (Matching) pour offrir une expérience complète.

---

## 🔐 Rôles et Permissions

Ce service implémente un contrôle d'accès basé sur les rôles (RBAC). Chaque utilisateur a un rôle qui détermine ses permissions.

### Rôles disponibles

| Rôle | Description |
|------|-------------|
| **CLIENT** | Utilisateur qui crée des demandes de transport pour ses marchandises |
| **PRESTATAIRE** | Transporteur qui exécute les missions de transport |
| **ADMIN** | Administrateur avec accès complet au système |

### Matrice des permissions

| Permission | CLIENT | PRESTATAIRE | ADMIN |
|------------|:------:|:-----------:|:-----:|
| Créer une demande de transport | ✅ | ❌ | ❌ |
| Voir ses propres demandes | ✅ | ✅ | ✅ |
| Valider une demande (accepter le devis) | ✅ | ❌ | ❌ |
| Voir TOUTES les demandes | ❌ | ❌ | ✅ |
| Filtrer les demandes par statut | ❌ | ❌ | ✅ |
| Voir les demandes d'une mission | ❌ | ✅ | ✅ |
| Associer mission/itinéraire à une demande | ❌ | ✅ | ✅ |
| Gérer les catégories (CRUD) | ❌ | ❌ | ✅ |
| Consulter les catégories (lecture) | ✅ | ✅ | ✅ |

### Règles d'accès détaillées

- **CLIENT** : 
  - Ne peut voir que **ses propres demandes** (celles qu'il a créées)
  - Peut créer de nouvelles demandes et les valider (accepter le devis)
  
- **PRESTATAIRE** :
  - Peut voir les demandes **associées à ses missions**
  - Peut voir les demandes **validées par les clients** (pour le matching)
  - Ne peut pas créer de demandes
  
- **ADMIN** :
  - Accès **complet** à toutes les demandes
  - Peut filtrer par statut, voir par mission, etc.
  - Gestion complète des catégories

---

## Sommaire

- [Quick Start](#quick-start)
- [Endpoints](#endpoints)
- [Modèles (DTOs)](#modèles-dtos)
- [Exemples de requêtes](#exemples-de-requêtes)
- [Authentification JWT](#authentification-jwt)
- [Health Check](#health-check)
- [Configuration](#configuration)
- [Docker](#docker)

---

## Quick Start

### 1. Lancer la base de données

```bash
docker-compose up -d postgres-demandes
```

### 2. Lancer l'application

```bash
./mvnw spring-boot:run
```

### 3. Vérifier que ça fonctionne

```bash
curl http://localhost:8083/actuator/health
```

### 4. Importer la collection Postman

Importez `Service3_Demandes_Transport.postman_collection.json` dans Postman - le token JWT est déjà configuré !

---

## Endpoints

### Demandes de Transport

| Méthode | Endpoint                              | Description                                      | Authentification | Rôle requis        |
| ------- | ------------------------------------- | ------------------------------------------------ | ---------------- | ------------------ |
| POST    | `/api/v1/demandes`                    | Créer une nouvelle demande de transport          | JWT requis       | CLIENT             |
| GET     | `/api/v1/demandes`                    | Lister toutes mes demandes (client authentifié)  | JWT requis       | CLIENT             |
| GET     | `/api/v1/demandes/{id}`               | Récupérer une demande par ID                     | JWT requis       | Tous (avec droits) |
| PUT     | `/api/v1/demandes/{id}/validation`    | Valider une demande (accepter le devis)          | JWT requis       | CLIENT             |
| PUT     | `/api/v1/demandes/{id}/association`   | Associer une mission et itinéraire à la demande  | JWT requis       | ADMIN/PRESTATAIRE  |
| GET     | `/api/v1/demandes/admin/all`          | Récupérer TOUTES les demandes                    | JWT requis       | ADMIN              |
| GET     | `/api/v1/demandes/admin/statut/{statut}` | Récupérer les demandes par statut             | JWT requis       | ADMIN              |
| GET     | `/api/v1/demandes/mission/{missionId}` | Récupérer les demandes d'une mission            | JWT requis       | PRESTATAIRE/ADMIN  |

> **Contrôle d'accès par rôle:**
> - **CLIENT** : Peut voir uniquement SES propres demandes
> - **PRESTATAIRE** : Peut voir les demandes de ses missions assignées + demandes validées
> - **ADMIN** : Peut voir TOUTES les demandes

### Matrice des Permissions

| Action | CLIENT | PRESTATAIRE | ADMIN |
|--------|--------|-------------|-------|
| Créer une demande | ✅ | ❌ | ❌ |
| Voir ses propres demandes | ✅ | ✅ | ✅ |
| Voir toutes les demandes | ❌ | ❌ | ✅ |
| Voir les demandes d'une mission | ❌ | ✅ | ✅ |
| Filtrer par statut | ❌ | ❌ | ✅ |
| Valider une demande (client) | ✅ | ❌ | ❌ |
| Associer mission/itinéraire | ❌ | ✅ | ✅ |

> **Note pour les autres microservices:** L'endpoint `PUT /api/v1/demandes/{id}/association` est destiné à être appelé par d'autres microservices (ex: Service Missions) pour associer une mission et un itinéraire à une demande existante.

### Catégories de Marchandise

| Méthode | Endpoint                                  | Description                            | Authentification |
| ------- | ----------------------------------------- | -------------------------------------- | ---------------- |
| GET     | `/api/v1/categories`                      | Lister toutes les catégories           | Non (public)     |
| GET     | `/api/v1/categories/{id}`                 | Récupérer une catégorie par ID (UUID)  | Non (public)     |
| GET     | `/api/v1/categories/nom/{nom}`            | Rechercher une catégorie par nom exact | Non (public)     |
| GET     | `/api/v1/categories/search?keyword=`      | Rechercher par mot-clé                 | Non (public)     |
| GET     | `/api/v1/categories/filter/fragile?=`     | Filtrer par fragilité                  | Non (public)     |
| GET     | `/api/v1/categories/filter/dangereux?=`   | Filtrer par dangerosité                | Non (public)     |
| GET     | `/api/v1/categories/filter/temperature?=` | Filtrer par température requise        | Non (public)     |
| POST    | `/api/v1/categories`                      | Créer une nouvelle catégorie           | JWT requis       |
| PUT     | `/api/v1/categories/{id}`                 | Modifier une catégorie                 | JWT requis       |
| DELETE  | `/api/v1/categories/{id}`                 | Supprimer une catégorie                | JWT requis       |

### Health & Monitoring

| Méthode | Endpoint                     | Description                | Authentification |
| ------- | ---------------------------- | -------------------------- | ---------------- |
| GET     | `/actuator/health`           | Health check               | Non              |
| GET     | `/actuator/health/liveness`  | Probe Kubernetes liveness  | Non              |
| GET     | `/actuator/health/readiness` | Probe Kubernetes readiness | Non              |

---

## Modèles (DTOs)

### `DemandeRequestDTO` (création de demande)

```json
{
  "volume": 15.5,
  "poids": 250.0,
  "natureMarchandise": "Meubles de salon",
  "dateDepart": "2025-12-15T10:00:00",
  "villeDepart": "Casablanca",
  "villeDestination": "Rabat",
  "categorieId": "cat-001-meubles"
}
```

| Champ              | Type          | Obligatoire | Validation                   |
| ------------------ | ------------- | ----------- | ---------------------------- |
| `volume`           | Double        | ✅          | Doit être positif            |
| `poids`            | Double        | ❌          | Poids en kg (optionnel)      |
| `natureMarchandise`| String        | ✅          | Non vide                     |
| `dateDepart`       | LocalDateTime | ✅          | Doit être dans le futur      |
| `villeDepart`      | String        | ✅          | Ville de départ              |
| `villeDestination` | String        | ✅          | Ville de destination         |
| `categorieId`      | String (UUID) | ❌          | ID d'une catégorie existante |

### `CategorieRequestDTO` (création de catégorie)

```json
{
  "nom": "Produits Alimentaires",
  "description": "Produits alimentaires nécessitant une chaîne de froid",
  "densiteMoyenne": 850.0,
  "fragile": false,
  "dangereux": false,
  "temperatureRequise": "refrigere",
  "restrictions": "Respecter la chaîne du froid"
}
```

| Champ                | Type    | Obligatoire | Validation                                      |
| -------------------- | ------- | ----------- | ----------------------------------------------- |
| `nom`                | String  | ✅          | Non vide, unique                                |
| `description`        | String  | ❌          | Max 500 caractères                              |
| `densiteMoyenne`     | Double  | ❌          | Doit être positif ou nul (kg/m³)                |
| `fragile`            | Boolean | ❌          | Par défaut: false                               |
| `dangereux`          | Boolean | ❌          | Par défaut: false                               |
| `temperatureRequise` | String  | ❌          | ambiante, refrigere, congele (défaut: ambiante) |
| `restrictions`       | String  | ❌          | Max 500 caractères                              |

### `CategorieResponseDTO` (réponse)

```json
{
  "idCategorie": "550e8400-e29b-41d4-a716-446655440000",
  "nom": "Produits Alimentaires",
  "description": "Produits alimentaires nécessitant une chaîne de froid",
  "densiteMoyenne": 850.0,
  "fragile": false,
  "dangereux": false,
  "temperatureRequise": "refrigere",
  "restrictions": "Respecter la chaîne du froid",
  "dateCreation": "2025-11-26T10:30:00",
  "dateModification": "2025-11-26T14:45:00"
}
```

### `DemandeResponseDTO` (réponse)

```json
{
  "id": 1,
  "clientId": 1,
  "volume": 15.5,
  "poids": 250.0,
  "natureMarchandise": "Meubles de salon",
  "dateDepart": "2025-12-15T10:00:00",
  "villeDepart": "Casablanca",
  "villeDestination": "Rabat",
  "statutValidation": "EN_ATTENTE_CLIENT",
  "devisEstime": 1500.0,
  "itineraireAssocieId": "550e8400-e29b-41d4-a716-446655440000",
  "distanceKm": 90.5,
  "dureeEstimeeMin": 75,
  "missionId": null,
  "categorie": {
    "idCategorie": "cat-001-meubles",
    "nom": "Meubles",
    "fragile": true,
    "temperatureRequise": "ambiante"
  },
  "dateCreation": "2025-11-25T22:30:00",
  "dateModification": "2025-11-25T22:30:00"
}
```

### `DemandeAssociationDTO` (association mission/itinéraire - pour autres microservices)

```json
{
  "missionId": 5,
  "itineraireId": "550e8400-e29b-41d4-a716-446655440000",
  "distanceKm": 133.7,
  "dureeEstimeeMin": 102
}
```

| Champ            | Type    | Obligatoire | Description                      |
| ---------------- | ------- | ----------- | -------------------------------- |
| `missionId`      | Long    | ✅          | ID de la mission à associer     |
| `itineraireId`   | String  | ❌          | ID de l'itinéraire (UUID)       |
| `distanceKm`     | Double  | ❌          | Distance en km                  |
| `dureeEstimeeMin`| Integer | ❌          | Durée estimée en minutes        |
```

### Catégories Prédéfinies

Le système inclut les catégories suivantes par défaut :

| ID                | Nom                       | Fragile | Dangereux | Température |
| ----------------- | ------------------------- | ------- | --------- | ----------- |
| `cat-001-meubles` | Meubles                   | ✅      | ❌        | ambiante    |
| `cat-002-electro` | Électroménager            | ✅      | ❌        | ambiante    |
| `cat-003-aliment` | Produits Alimentaires     | ❌      | ❌        | refrigere   |
| `cat-004-surgele` | Produits Surgelés         | ❌      | ❌        | congele     |
| `cat-005-constr`  | Matériaux de Construction | ❌      | ❌        | ambiante    |
| `cat-006-chimiq`  | Produits Chimiques        | ❌      | ✅        | ambiante    |
| `cat-007-pharma`  | Produits Pharmaceutiques  | ✅      | ❌        | refrigere   |
| `cat-008-texti`   | Textiles                  | ❌      | ❌        | ambiante    |

### Énumérations

#### StatutValidation

| Valeur                | Description                             |
| --------------------- | --------------------------------------- |
| `EN_ATTENTE_CLIENT`   | Demande créée, en attente de validation |
| `VALIDEE_CLIENT`      | Demande validée par le client           |
| `VALIDEE_PRESTATAIRE` | Validée par le prestataire              |
| `TERMINEE`            | Demande terminée                        |
| `ANNULEE`             | Demande annulée                         |

---

## Exemples de requêtes

### 1. Créer une demande de transport

```http
POST /api/v1/demandes
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "volume": 15.5,
  "poids": 250.0,
  "natureMarchandise": "Meubles de salon",
  "dateDepart": "2025-12-15T10:00:00",
  "villeDepart": "Casablanca",
  "villeDestination": "Rabat"
}
```

**Réponse (201 Created):**

```json
{
  "id": 1,
  "clientId": 1,
  "volume": 15.5,
  "poids": 250.0,
  "natureMarchandise": "Meubles de salon",
  "villeDepart": "Casablanca",
  "villeDestination": "Rabat",
  "statutValidation": "EN_ATTENTE_CLIENT",
  "devisEstime": 1500.00,
  ...
}
```

### 2. Lister mes demandes

```http
GET /api/v1/demandes
Authorization: Bearer <jwt_token>
```

### 3. Voir une demande spécifique

```http
GET /api/v1/demandes/1
Authorization: Bearer <jwt_token>
```

### 4. Valider une demande (accepter le devis)

```http
PUT /api/v1/demandes/1/validation
Authorization: Bearer <jwt_token>
```

**Réponse (200 OK):**

```json
{
  "id": 1,
  "statutValidation": "VALIDEE_CLIENT",
  ...
}
```

### 5. Associer une mission et un itinéraire (pour autres microservices)

```http
PUT /api/v1/demandes/1/association
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "missionId": 5,
  "itineraireId": "550e8400-e29b-41d4-a716-446655440000",
  "distanceKm": 90.5,
  "dureeEstimeeMin": 75
}
```

**Réponse (200 OK):**

```json
{
  "id": 1,
  "missionId": 5,
  "itineraireAssocieId": "550e8400-e29b-41d4-a716-446655440000",
  "distanceKm": 90.5,
  "dureeEstimeeMin": 75,
  ...
}
```

> **Note:** Cet endpoint est destiné à être appelé par d'autres microservices (ex: Service Missions) pour associer une mission et un itinéraire à une demande existante.

### 6. [ADMIN] Récupérer TOUTES les demandes

```http
GET /api/v1/demandes/admin/all
Authorization: Bearer <admin_jwt_token>
```

**Réponse (200 OK):**

```json
[
  {
    "id": 1,
    "clientId": 1,
    "volume": 15.5,
    "statutValidation": "EN_ATTENTE_CLIENT",
    ...
  },
  {
    "id": 2,
    "clientId": 2,
    "volume": 25.0,
    "statutValidation": "VALIDEE_CLIENT",
    ...
  }
]
```

> **Note:** Nécessite un token JWT avec `role=ADMIN`.

### 7. [ADMIN] Récupérer les demandes par statut

```http
GET /api/v1/demandes/admin/statut/VALIDEE_CLIENT
Authorization: Bearer <admin_jwt_token>
```

**Statuts disponibles:**
- `EN_ATTENTE_CLIENT` - En attente de validation client
- `VALIDEE_CLIENT` - Validée par le client
- `VALIDEE_PRESTATAIRE` - Validée par le prestataire
- `TERMINEE` - Demande terminée
- `ANNULEE` - Demande annulée

### 8. [PRESTATAIRE/ADMIN] Récupérer les demandes d'une mission

```http
GET /api/v1/demandes/mission/5
Authorization: Bearer <prestataire_jwt_token>
```

**Réponse (200 OK):**

```json
[
  {
    "id": 3,
    "clientId": 1,
    "missionId": 5,
    "statutValidation": "VALIDEE_CLIENT",
    ...
  }
]
```

> **Note:** Accessible aux prestataires (pour leurs missions) et aux administrateurs.

---

## Authentification JWT

### Configuration (compatible avec Service Utilisateurs - Microservice 1)

Ce service utilise la même clé JWT que le Service Utilisateurs pour permettre le Single Sign-On (SSO) entre microservices.

| Paramètre | Valeur |
|-----------|--------|
| **Algorithme** | HS256 |
| **Secret** | `transport_marchandises_api2025vs2026` |
| **Header** | `Authorization: Bearer <token>` |
| **Rôles supportés** | `CLIENT`, `PRESTATAIRE`, `ADMIN` |

### Obtenir un token depuis le Service Utilisateurs

```http
POST http://172.30.80.11:31019/account/login/
Content-Type: application/json

{
  "email": "votre_email@example.com",
  "password": "votre_mot_de_passe"
}
```

**Réponse:**
```json
{
  "access": "<jwt_token>",
  "refresh": "<refresh_token>"
}
```

### Utiliser le token avec ce service

```http
GET http://172.30.80.11:31029/api/v1/demandes
Authorization: Bearer <access_token>
```

### Générer des tokens de test (développement uniquement)

```bash
./mvnw compile exec:java "-Dexec.mainClass=ma.tna.microservice3.util.JwtTokenGenerator"
```

### Format du header

```
Authorization: Bearer <token>
```

### Payload JWT attendu

```json
{
  "sub": "user@email.com",
  "user_id": 1,
  "userId": 1,
  "role": "CLIENT",
  "user_type": "CLIENT",
  "email": "user@email.com",
  "iat": 1764108648,
  "exp": 1795644648
}
```

> **Note:** Le service supporte plusieurs formats de claims (`userId`/`user_id`, `role`/`user_type`) pour la compatibilité avec différents générateurs de tokens.

---

## Health Check

### Vérifier la santé de l'application

```http
GET /actuator/health
```

**Réponse:**

```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

### Probes Kubernetes

```http
GET /actuator/health/liveness   → Application vivante ?
GET /actuator/health/readiness  → Application prête ?
```

---

## Configuration

### Variables d'environnement

| Variable                     | Description                 | Valeur par défaut                              |
| ---------------------------- | --------------------------- | ---------------------------------------------- | 
| `SERVER_PORT`                | Port de l'application       | `8083`                                         |
| `SPRING_DATASOURCE_URL`      | URL de connexion PostgreSQL | `jdbc:postgresql://localhost:5433/demandes_db` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur PostgreSQL      | `demandes_user`                                |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe PostgreSQL     | `demandes_password`                            |
| `JWT_SECRET`                 | Clé secrète JWT (texte brut)| `transport_marchandises_api2025vs2026`         |
| `SERVICE_URL_UTILISATEURS`   | URL Service Utilisateurs    | `http://172.30.80.11:31019/account`            |
| `SERVICE_URL_ITINERAIRES`    | URL Service Itinéraires     | `http://172.30.80.11:31030/api/routes`         |
| `SERVICE_URL_TARIFICATION`   | URL Service Tarification    | `http://localhost:8085/api/v1/tarifs`          |
| `SERVICE_URL_MATCHING`       | URL Service Matching        | `http://localhost:8088/api/v1/matching`        |

---

## Docker

### Lancer uniquement la base de données

```bash
docker-compose up -d postgres-demandes
```

### Lancer tout (application + base de données)

```bash
docker-compose up -d
```

### Build et lancer (première fois ou après modifications)

```bash
docker-compose up --build -d
```

### Rebuild sans cache (si problèmes de cache)

```bash
docker-compose build --no-cache service-demandes
docker-compose up -d
```

### Redémarrer les services

```bash
# Redémarrer tous les services
docker-compose restart

# Redémarrer uniquement l'application
docker-compose restart service-demandes
```

### Arrêter les services

```bash
# Arrêter (garde les conteneurs)
docker-compose stop

# Arrêter et supprimer les conteneurs
docker-compose down

# Arrêter et supprimer tout (volumes inclus)
docker-compose down -v
```

### Voir les logs

```bash
# Tous les logs
docker-compose logs -f

# Logs de l'application uniquement
docker-compose logs -f service-demandes

# Dernières 100 lignes
docker-compose logs --tail=100 service-demandes
```

### Vérifier l'état des conteneurs

```bash
docker-compose ps
```

### Accéder à PostgreSQL

```bash
docker exec -it demandes_db_ms3 psql -U demandes_user -d demandes_db
```

### Commandes utiles

```bash
# Voir les images construites
docker images | grep microservice3

# Supprimer l'image pour forcer un rebuild complet
docker rmi microservice3-service-demandes

# Nettoyer les ressources Docker non utilisées
docker system prune -f
```

---

## Architecture

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Service 1     │     │   Service 4     │     │   Service 5     │
│  (Utilisateurs) │     │  (Itinéraires)  │     │ (Tarification)  │
│     :8081       │     │     :8084       │     │     :8085       │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │ JWT Token             │ Calcul Route          │ Calcul Devis
         ▼                       ▼                       ▼
┌────────────────────────────────────────────────────────────────────┐
│                        SERVICE 3 - DEMANDES                        │
│                           (Ce Service)                             │
│                             :8083                                  │
└────────────────────────────────────────────────────────────────────┘
                                 │
                                 │ Matching Transporteur
                                 ▼
                        ┌─────────────────┐
                        │   Service 8     │
                        │   (Matching)    │
                        │     :8088       │
                        └─────────────────┘
```

---

## Collection Postman

Le fichier `Service3_Demandes_Transport.postman_collection.json` contient :

- 🏥 **Health & Status** - Endpoints de monitoring
- 📦 **Demandes CRUD** - Créer, lister, voir, valider
- 📝 **Exemples** - Différents types de demandes
- 🔒 **Tests Sécurité** - Vérification authentification

**Le token JWT est pré-configuré** - importez et testez directement !

---

## Technologies

| Technologie     | Version | Usage                 |
| --------------- | ------- | --------------------- |
| Java            | 21      | Langage               |
| Spring Boot     | 3.5.8   | Framework             |
| Spring Security | 6.x     | Authentification JWT  |
| Spring Data JPA | 3.x     | Accès base de données |
| PostgreSQL      | 16      | Base de données       |
| Docker          | -       | Containerisation      |
| Maven           | 3.9+    | Build                 |
| JJWT            | 0.12.3  | Gestion tokens JWT    |
| Lombok          | -       | Réduction boilerplate |

---

## Auteur

**MicroService3** - Service Demandes de Transport
Fait partie de l'architecture microservices de Transport Maroc.
```
