# 🎯 GUIDE RAPIDE - Tester l'Application

## ✅ L'APPLICATION FONCTIONNE !

**Status** : ✅ OPERATIONAL  
**URL** : http://localhost:8083  
**Temps de démarrage** : 6.768 secondes  

---

## 🧪 Tests Immédiats (Sans JWT)

### 1. Health Check
```bash
curl http://localhost:8083/actuator/health
```
**Résultat** : ✅ `{"status":"UP","groups":["liveness","readiness"]}`

### 2. Liveness Probe
```bash
curl http://localhost:8083/actuator/health/liveness
```

### 3. Readiness Probe
```bash
curl http://localhost:8083/actuator/health/readiness
```

### 4. Métriques
```bash
curl http://localhost:8083/actuator/metrics
```

---

## 🔐 Tester les Endpoints REST (Nécessite JWT)

### ⚠️ Prérequis
Vous avez besoin d'un **token JWT valide** du Service 1 (Utilisateurs).

Si vous n'avez pas encore le Service 1, vous pouvez :
1. Générer un token JWT test sur https://jwt.io
2. Utiliser le secret configuré : `your-secret-key-min-256-bits-long-change-this-in-production`

### Exemple de Token JWT Test
Payload minimum requis :
```json
{
  "sub": "1",
  "userId": 1,
  "role": "ROLE_CLIENT",
  "exp": 9999999999
}
```

---

## 📮 Tester avec Postman (Recommandé)

### 1. Importer la Collection
1. Ouvrir Postman
2. File → Import
3. Sélectionner : `Service3_Demandes_Transport.postman_collection.json`

### 2. Configurer le Token JWT
1. Cliquer sur la collection
2. Variables → `jwt_token`
3. Coller votre token JWT
4. Save

### 3. Tester les Endpoints

#### Créer une Demande
```
POST http://localhost:8083/api/v1/demandes
Headers: Authorization: Bearer {{jwt_token}}
Body:
{
  "volume": 15.5,
  "natureMarchandise": "Meubles",
  "dateDepart": "2025-12-15T10:00:00",
  "adresseDepart": "123 Rue Example, Paris",
  "adresseDestination": "456 Avenue Test, Lyon"
}
```

**Résultat attendu** : 
- Status: 201 Created
- Demande créée avec ID, devis estimé, itinéraire

#### Lister les Demandes
```
GET http://localhost:8083/api/v1/demandes
Headers: Authorization: Bearer {{jwt_token}}
```

**Résultat attendu** :
- Status: 200 OK
- Liste des demandes du client

---

## 🗄️ Vérifier PostgreSQL

```bash
# Se connecter
psql -U postgres -d demandes_db

# Lister les tables
\dt

# Voir les demandes
SELECT * FROM demandes;
```

---

## 📊 Ce qui Fonctionne

✅ **Application démarrée** (6.7 secondes)  
✅ **PostgreSQL connecté** (demandes_db)  
✅ **Tables créées** (demandes)  
✅ **Spring Security actif** (JWT)  
✅ **Actuator endpoints** (health, metrics)  
✅ **Prêt pour les tests**  

---

## ⚠️ Notes Importantes

### 403 Forbidden sur `/`
C'est **NORMAL** ! La racine est protégée par Spring Security.

Les endpoints disponibles sont :
- `/actuator/*` - Sans JWT ✅
- `/api/v1/demandes/*` - Avec JWT 🔒

### Pour Tester Sans Service 1
Vous pouvez créer un token JWT temporaire :
1. Aller sur https://jwt.io
2. Utiliser le secret : `your-secret-key-min-256-bits-long-change-this-in-production`
3. Payload :
```json
{
  "sub": "1",
  "userId": 1,
  "role": "ROLE_CLIENT",
  "iat": 1730739600,
  "exp": 9999999999
}
```

---

## 🎯 Prochaines Étapes

1. ✅ **Créer une demande** avec Postman
2. ✅ **Vérifier dans PostgreSQL** que la demande est enregistrée
3. ✅ **Lister les demandes** pour voir le résultat
4. ✅ **Valider une demande** pour tester le workflow complet
5. 🔜 **Intégrer avec Service 1** pour avoir de vrais tokens JWT
6. 🔜 **Intégrer avec Services 4, 5, 7, 8** pour le flux complet

---

## 🆘 Besoin d'Aide ?

- **Guide complet** : GUIDE_TEST.md
- **Documentation** : README.md
- **Démarrage rapide** : QUICKSTART.md
- **Collection Postman** : Service3_Demandes_Transport.postman_collection.json

---

## ✅ Validation

L'application est **OPÉRATIONNELLE** et prête à être testée ! 🚀

**Commencez par** :
1. Ouvrir Postman
2. Importer la collection
3. Configurer un token JWT
4. Créer votre première demande

**Bon test !** 🎉

