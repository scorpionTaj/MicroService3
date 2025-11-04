# Service 3 : Service Demandes de Transport

## Description
Ce microservice gère les demandes de transport dans le système de logistique. Il permet aux clients de créer des demandes de transport, d'obtenir des devis estimés, et de valider leurs demandes.

## Technologies Utilisées
- **Java 25**
- **Spring Boot 4**
- **Spring Data JPA** (avec PostgreSQL)
- **Spring Security 6+** (avec JWT)
- **Spring WebFlux** (WebClient pour les appels inter-services)
- **Maven** (gestion des dépendances)
- **Lombok** (réduction du code boilerplate)
- **JJWT 0.12.3** (gestion des tokens JWT)

## Fonctionnalités Principales

### 1. Gestion des Demandes
- ✅ Création de demandes de transport
- ✅ Validation des demandes par les clients
- ✅ Récupération des demandes (individuelle ou liste)
- ✅ Mise à jour du statut de paiement (webhook)

### 2. Intégrations Inter-Services
- **Service 4 - Itinéraires** : Calcul automatique de l'itinéraire optimal
- **Service 5 - Tarification** : Calcul du devis estimé
- **Service 7 - Paiements** : Initiation des paiements après validation
- **Service 8 - Matching** : Recherche de transporteurs disponibles

### 3. Sécurité
- Authentification par JWT (tokens générés par le Service 1 - Utilisateurs)
- Protection des endpoints avec Spring Security
- Validation des autorisations (un client ne peut accéder qu'à ses propres demandes)

## Structure du Projet

```
src/main/java/ma/tna/microservice3/
├── config/
│   ├── SecurityConfig.java           # Configuration Spring Security
│   └── WebClientConfig.java          # Configuration WebClient
├── controller/
│   └── DemandeController.java        # Endpoints REST
├── dto/
│   ├── DemandeRequestDTO.java        # DTO pour créer une demande
│   ├── DemandeResponseDTO.java       # DTO de réponse
│   ├── PaiementStatusUpdateDTO.java  # DTO pour mise à jour paiement
│   ├── ItineraireResponseDTO.java    # DTO itinéraire
│   └── TarifResponseDTO.java         # DTO tarif
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java   # Gestion globale des erreurs
├── mapper/
│   └── DemandeMapper.java            # Conversion Entity <-> DTO
├── model/
│   ├── Demande.java                  # Entité JPA
│   ├── StatutValidation.java         # Enum des statuts de validation
│   └── StatutPaiement.java           # Enum des statuts de paiement
├── repository/
│   └── DemandeRepository.java        # Repository Spring Data JPA
├── security/
│   └── JwtAuthFilter.java            # Filtre d'authentification JWT
├── service/
│   ├── DemandeService.java           # Interface du service
│   └── DemandeServiceImpl.java       # Implémentation du service
├── util/
│   └── JwtUtil.java                  # Utilitaire JWT
└── MicroService3Application.java     # Classe principale
```

## Configuration

### Base de Données (PostgreSQL)

**⚠️ IMPORTANT** : Voir le guide détaillé **[DATABASE_SETUP.md](DATABASE_SETUP.md)** pour la configuration complète.

#### Méthode Rapide (Automatique)
Utilisez le script PowerShell fourni :
```bash
.\setup-database.ps1
```

#### Méthode Simple (Manuel)
1. Créez la base de données :
```sql
CREATE DATABASE demandes_db;
```

2. Exécutez le script SQL :
```bash
psql -U postgres -d demandes_db -f src\main\resources\schema.sql
```

#### Méthode Ultra-Simple (Hibernate)
Créez juste la base de données et laissez Hibernate créer les tables :
```sql
CREATE DATABASE demandes_db;
```
Puis démarrez l'application avec `.\mvnw.cmd spring-boot:run`

### Application Properties
Mettez à jour `src/main/resources/application.properties` avec vos paramètres :

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/demandes_db
spring.datasource.username=votre_username
spring.datasource.password=votre_password

# JWT
jwt.secret=votre-secret-key-min-256-bits-long
jwt.expiration=86400000

# URLs des autres services
service.url.itineraires=http://localhost:8084/api/v1/itineraires
service.url.tarification=http://localhost:8085/api/v1/tarifs
service.url.matching=http://localhost:8088/api/v1/matching
service.url.paiements=http://localhost:8087/api/v1/paiements
```

## API Endpoints

### Créer une Demande
```http
POST /api/v1/demandes
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "volume": 15.5,
  "natureMarchandise": "Meubles",
  "dateDepart": "2024-12-15T10:00:00",
  "adresseDepart": "123 Rue Example, Paris",
  "adresseDestination": "456 Avenue Test, Lyon"
}
```

### Valider une Demande (Client)
```http
PUT /api/v1/demandes/{id}/validation
Authorization: Bearer {jwt_token}
```

### Récupérer toutes mes Demandes
```http
GET /api/v1/demandes
Authorization: Bearer {jwt_token}
```

### Récupérer une Demande par ID
```http
GET /api/v1/demandes/{id}
Authorization: Bearer {jwt_token}
```

### Webhook - Mise à jour Statut Paiement
```http
PUT /api/v1/demandes/{id}/paiement
Content-Type: application/json

{
  "nouveauStatut": "PAYEE"
}
```

## Modèle de Données

### Entité Demande
```java
- id: Long (PK)
- clientId: Long (ID du Service 1)
- volume: Double
- natureMarchandise: String
- dateDepart: LocalDateTime
- adresseDepart: String
- adresseDestination: String
- statutValidation: StatutValidation (EN_ATTENTE_CLIENT, VALIDEE_CLIENT, etc.)
- statutPaiement: StatutPaiement (EN_ATTENTE, PAYEE, REMBOURSEE)
- itineraireAssocieId: Long (ID du Service 4)
- groupeId: Long (pour le regroupement)
- devisEstime: BigDecimal
- dateCreation: LocalDateTime
- dateModification: LocalDateTime
```

## Compilation et Exécution

### Compiler le projet
```bash
./mvnw clean compile
```

### Exécuter les tests
```bash
./mvnw test
```

### Lancer l'application
```bash
./mvnw spring-boot:run
```

L'application démarre sur le port **8083**.

## Dépendances Principales

```xml
<!-- Spring Boot Starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## Flux de Traitement d'une Demande

1. **Création de la Demande**
   - Client envoie une demande avec les détails du transport
   - Le service sauvegarde la demande (statut: EN_ATTENTE_CLIENT)
   - Appel asynchrone au Service Itinéraires pour calculer le trajet
   - Appel asynchrone au Service Tarification pour obtenir un devis
   - Mise à jour de la demande avec l'itinéraire et le devis
   - Retour de la demande complète au client

2. **Validation de la Demande**
   - Client valide la demande (accepte le devis)
   - Mise à jour du statut à VALIDEE_CLIENT
   - Appel asynchrone au Service Matching pour trouver un transporteur
   - Appel asynchrone au Service Paiements pour initier le paiement

3. **Mise à jour du Paiement**
   - Le Service Paiements notifie via webhook
   - Mise à jour du statutPaiement (PAYEE, REMBOURSEE, etc.)

## Sécurité JWT

Le service utilise un filtre JWT (`JwtAuthFilter`) qui :
- Intercepte chaque requête
- Vérifie le token JWT dans le header `Authorization: Bearer {token}`
- Valide le token (signature, expiration)
- Extrait les informations (userId, role)
- Peuple le `SecurityContext` pour l'autorisation

## Notes de Développement

### Utilisation des Java Records
Les DTOs utilisent des **Java Records** (Java 14+) pour l'immuabilité et la concision :
```java
public record DemandeRequestDTO(
    @NotNull Double volume,
    @NotBlank String natureMarchandise,
    // ...
) {}
```

### WebClient (Non-Bloquant)
Les appels inter-services utilisent `WebClient` de Spring WebFlux pour des performances optimales :
```java
webClient.post()
    .uri(serviceUrl)
    .bodyValue(request)
    .retrieve()
    .bodyToMono(ResponseDTO.class)
    .block(); // ou .subscribe() pour vraiment asynchrone
```

### Gestion des Erreurs
Un `GlobalExceptionHandler` capture toutes les exceptions et renvoie des réponses HTTP appropriées.

## Auteur
Développé dans le cadre d'une architecture microservices pour un système de gestion de transport.

## Licence
Ce projet est destiné à un usage éducatif et professionnel.

