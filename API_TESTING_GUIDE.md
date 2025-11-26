# MicroService 3 - API Testing Guide

## Service Demandes Transport

**Base URL:** `http://localhost:8083`  
**API Version:** v1

---

## 🔐 Authentication

All endpoints (except webhook) require a JWT Bearer token in the Authorization header.

```
Authorization: Bearer <your-jwt-token>
```

---

## 📋 Endpoints Summary

| Method | Endpoint                           | Description                         |
| ------ | ---------------------------------- | ----------------------------------- |
| `GET`  | `/api/v1/demandes`                 | Get all client's transport requests |
| `POST` | `/api/v1/demandes`                 | Create a new transport request      |
| `GET`  | `/api/v1/demandes/{id}`            | Get a specific request by ID        |
| `PUT`  | `/api/v1/demandes/{id}/validation` | Validate/Confirm a request          |
| `PUT`  | `/api/v1/demandes/{id}/paiement`   | Update payment status (webhook)     |

---

## 🧪 API Testing Examples

### Variables Setup

```
BASE_URL = http://localhost:8083
JWT_TOKEN = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 1️⃣ Create a Transport Request

### PowerShell

```powershell
# Create a new transport request
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
    "Content-Type" = "application/json"
}

$body = @{
    volume = 25.5
    natureMarchandise = "Matériaux de construction"
    dateDepart = "2025-12-15T08:00:00"
    adresseDepart = "123 Rue Mohammed V, Casablanca"
    adresseDestination = "456 Avenue Hassan II, Rabat"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/demandes" `
    -Method POST `
    -Headers $headers `
    -Body $body

$response | ConvertTo-Json -Depth 10
```

### cURL

```bash
curl -X POST "http://localhost:8083/api/v1/demandes" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "volume": 25.5,
    "natureMarchandise": "Matériaux de construction",
    "dateDepart": "2025-12-15T08:00:00",
    "adresseDepart": "123 Rue Mohammed V, Casablanca",
    "adresseDestination": "456 Avenue Hassan II, Rabat"
  }'
```

### JavaScript Fetch

```javascript
const createDemande = async () => {
  const response = await fetch("http://localhost:8083/api/v1/demandes", {
    method: "POST",
    headers: {
      Authorization: "Bearer YOUR_JWT_TOKEN",
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      volume: 25.5,
      natureMarchandise: "Matériaux de construction",
      dateDepart: "2025-12-15T08:00:00",
      adresseDepart: "123 Rue Mohammed V, Casablanca",
      adresseDestination: "456 Avenue Hassan II, Rabat",
    }),
  });

  const data = await response.json();
  console.log("Created:", data);
  return data;
};

createDemande();
```

---

## 2️⃣ Get All Client's Requests

### PowerShell

```powershell
# Get all transport requests for authenticated client
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/demandes" `
    -Method GET `
    -Headers $headers

$response | ConvertTo-Json -Depth 10
```

### cURL

```bash
curl -X GET "http://localhost:8083/api/v1/demandes" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### JavaScript Fetch

```javascript
const getAllDemandes = async () => {
  const response = await fetch("http://localhost:8083/api/v1/demandes", {
    method: "GET",
    headers: {
      Authorization: "Bearer YOUR_JWT_TOKEN",
    },
  });

  const data = await response.json();
  console.log("All Demandes:", data);
  return data;
};

getAllDemandes();
```

---

## 3️⃣ Get Request by ID

### PowerShell

```powershell
# Get a specific transport request by ID
$demandeId = 1
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/demandes/$demandeId" `
    -Method GET `
    -Headers $headers

$response | ConvertTo-Json -Depth 10
```

### cURL

```bash
curl -X GET "http://localhost:8083/api/v1/demandes/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### JavaScript Fetch

```javascript
const getDemandeById = async (id) => {
  const response = await fetch(`http://localhost:8083/api/v1/demandes/${id}`, {
    method: "GET",
    headers: {
      Authorization: "Bearer YOUR_JWT_TOKEN",
    },
  });

  const data = await response.json();
  console.log("Demande:", data);
  return data;
};

getDemandeById(1);
```

---

## 4️⃣ Validate a Request (Client Confirmation)

### PowerShell

```powershell
# Validate/Confirm a transport request
$demandeId = 1
$headers = @{
    "Authorization" = "Bearer YOUR_JWT_TOKEN"
}

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/demandes/$demandeId/validation" `
    -Method PUT `
    -Headers $headers

$response | ConvertTo-Json -Depth 10
```

### cURL

```bash
curl -X PUT "http://localhost:8083/api/v1/demandes/1/validation" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### JavaScript Fetch

```javascript
const validerDemande = async (id) => {
  const response = await fetch(
    `http://localhost:8083/api/v1/demandes/${id}/validation`,
    {
      method: "PUT",
      headers: {
        Authorization: "Bearer YOUR_JWT_TOKEN",
      },
    }
  );

  const data = await response.json();
  console.log("Validated:", data);
  return data;
};

validerDemande(1);
```

---

## 5️⃣ Update Payment Status (Webhook)

This endpoint is called by the Payment Service to update payment status.

### PowerShell

```powershell
# Update payment status (webhook from Payment Service)
$demandeId = 1
$headers = @{
    "Content-Type" = "application/json"
}

$body = @{
    nouveauStatut = "PAYEE"  # Values: EN_ATTENTE, PAYEE, REMBOURSEE
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/demandes/$demandeId/paiement" `
    -Method PUT `
    -Headers $headers `
    -Body $body

Write-Host "Payment status updated successfully"
```

### cURL

```bash
# Update to PAYEE (Paid)
curl -X PUT "http://localhost:8083/api/v1/demandes/1/paiement" \
  -H "Content-Type: application/json" \
  -d '{
    "nouveauStatut": "PAYEE"
  }'

# Update to REMBOURSEE (Refunded)
curl -X PUT "http://localhost:8083/api/v1/demandes/1/paiement" \
  -H "Content-Type: application/json" \
  -d '{
    "nouveauStatut": "REMBOURSEE"
  }'
```

### JavaScript Fetch

```javascript
const updatePaymentStatus = async (id, status) => {
  const response = await fetch(
    `http://localhost:8083/api/v1/demandes/${id}/paiement`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        nouveauStatut: status, // 'EN_ATTENTE', 'PAYEE', 'REMBOURSEE'
      }),
    }
  );

  if (response.ok) {
    console.log("Payment status updated successfully");
  } else {
    console.error("Failed to update payment status");
  }
};

updatePaymentStatus(1, "PAYEE");
```

---

## 📊 Complete Test Script Examples

### PowerShell Complete Test Script

```powershell
# ============================================
# MicroService 3 - Complete API Test Script
# ============================================

$baseUrl = "http://localhost:8083"
$jwtToken = "YOUR_JWT_TOKEN"

$headers = @{
    "Authorization" = "Bearer $jwtToken"
    "Content-Type" = "application/json"
}

Write-Host "=== MicroService 3 API Tests ===" -ForegroundColor Cyan

# Test 1: Create a new transport request
Write-Host "`n[TEST 1] Creating new transport request..." -ForegroundColor Yellow
$createBody = @{
    volume = 30.0
    natureMarchandise = "Équipements électroniques"
    dateDepart = "2025-12-20T09:00:00"
    adresseDepart = "Zone Industrielle, Tanger"
    adresseDestination = "Quartier Industriel, Marrakech"
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri "$baseUrl/api/v1/demandes" `
        -Method POST `
        -Headers $headers `
        -Body $createBody

    Write-Host "✓ Request created successfully! ID: $($created.id)" -ForegroundColor Green
    $demandeId = $created.id
} catch {
    Write-Host "✗ Failed to create request: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# Test 2: Get all requests
Write-Host "`n[TEST 2] Getting all requests..." -ForegroundColor Yellow
try {
    $allDemandes = Invoke-RestMethod -Uri "$baseUrl/api/v1/demandes" `
        -Method GET `
        -Headers $headers

    Write-Host "✓ Retrieved $($allDemandes.Count) request(s)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to get requests: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Get specific request by ID
Write-Host "`n[TEST 3] Getting request by ID: $demandeId..." -ForegroundColor Yellow
try {
    $demande = Invoke-RestMethod -Uri "$baseUrl/api/v1/demandes/$demandeId" `
        -Method GET `
        -Headers $headers

    Write-Host "✓ Request details retrieved" -ForegroundColor Green
    Write-Host "  - Status: $($demande.statutValidation)" -ForegroundColor Gray
    Write-Host "  - Payment: $($demande.statutPaiement)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Failed to get request: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Validate the request
Write-Host "`n[TEST 4] Validating request ID: $demandeId..." -ForegroundColor Yellow
try {
    $validated = Invoke-RestMethod -Uri "$baseUrl/api/v1/demandes/$demandeId/validation" `
        -Method PUT `
        -Headers $headers

    Write-Host "✓ Request validated! Status: $($validated.statutValidation)" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to validate request: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Update payment status
Write-Host "`n[TEST 5] Updating payment status to PAYEE..." -ForegroundColor Yellow
$paymentBody = @{
    nouveauStatut = "PAYEE"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$baseUrl/api/v1/demandes/$demandeId/paiement" `
        -Method PUT `
        -Headers @{"Content-Type" = "application/json"} `
        -Body $paymentBody

    Write-Host "✓ Payment status updated successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to update payment: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== All Tests Completed ===" -ForegroundColor Cyan
```

### Bash/cURL Complete Test Script

```bash
#!/bin/bash
# ============================================
# MicroService 3 - Complete API Test Script
# ============================================

BASE_URL="http://localhost:8083"
JWT_TOKEN="YOUR_JWT_TOKEN"

echo "=== MicroService 3 API Tests ==="

# Test 1: Create a new transport request
echo -e "\n[TEST 1] Creating new transport request..."
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/demandes" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "volume": 30.0,
    "natureMarchandise": "Équipements électroniques",
    "dateDepart": "2025-12-20T09:00:00",
    "adresseDepart": "Zone Industrielle, Tanger",
    "adresseDestination": "Quartier Industriel, Marrakech"
  }')

DEMANDE_ID=$(echo $CREATE_RESPONSE | jq -r '.id')
echo "✓ Request created! ID: $DEMANDE_ID"
echo "Response: $CREATE_RESPONSE" | jq .

# Test 2: Get all requests
echo -e "\n[TEST 2] Getting all requests..."
curl -s -X GET "$BASE_URL/api/v1/demandes" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .

# Test 3: Get specific request by ID
echo -e "\n[TEST 3] Getting request by ID: $DEMANDE_ID..."
curl -s -X GET "$BASE_URL/api/v1/demandes/$DEMANDE_ID" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .

# Test 4: Validate the request
echo -e "\n[TEST 4] Validating request ID: $DEMANDE_ID..."
curl -s -X PUT "$BASE_URL/api/v1/demandes/$DEMANDE_ID/validation" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .

# Test 5: Update payment status
echo -e "\n[TEST 5] Updating payment status to PAYEE..."
curl -s -X PUT "$BASE_URL/api/v1/demandes/$DEMANDE_ID/paiement" \
  -H "Content-Type: application/json" \
  -d '{"nouveauStatut": "PAYEE"}'

echo -e "\n=== All Tests Completed ==="
```

### JavaScript/Node.js Complete Test Script

```javascript
// ============================================
// MicroService 3 - Complete API Test Script
// ============================================

const BASE_URL = "http://localhost:8083";
const JWT_TOKEN = "YOUR_JWT_TOKEN";

const headers = {
  Authorization: `Bearer ${JWT_TOKEN}`,
  "Content-Type": "application/json",
};

async function runTests() {
  console.log("=== MicroService 3 API Tests ===\n");

  let demandeId;

  // Test 1: Create a new transport request
  console.log("[TEST 1] Creating new transport request...");
  try {
    const createResponse = await fetch(`${BASE_URL}/api/v1/demandes`, {
      method: "POST",
      headers,
      body: JSON.stringify({
        volume: 30.0,
        natureMarchandise: "Équipements électroniques",
        dateDepart: "2025-12-20T09:00:00",
        adresseDepart: "Zone Industrielle, Tanger",
        adresseDestination: "Quartier Industriel, Marrakech",
      }),
    });

    const created = await createResponse.json();
    demandeId = created.id;
    console.log(`✓ Request created! ID: ${demandeId}`);
    console.log("Response:", JSON.stringify(created, null, 2));
  } catch (error) {
    console.error("✗ Failed:", error.message);
    return;
  }

  // Test 2: Get all requests
  console.log("\n[TEST 2] Getting all requests...");
  try {
    const response = await fetch(`${BASE_URL}/api/v1/demandes`, {
      method: "GET",
      headers,
    });
    const demandes = await response.json();
    console.log(`✓ Retrieved ${demandes.length} request(s)`);
  } catch (error) {
    console.error("✗ Failed:", error.message);
  }

  // Test 3: Get specific request by ID
  console.log(`\n[TEST 3] Getting request by ID: ${demandeId}...`);
  try {
    const response = await fetch(`${BASE_URL}/api/v1/demandes/${demandeId}`, {
      method: "GET",
      headers,
    });
    const demande = await response.json();
    console.log("✓ Request details:", JSON.stringify(demande, null, 2));
  } catch (error) {
    console.error("✗ Failed:", error.message);
  }

  // Test 4: Validate the request
  console.log(`\n[TEST 4] Validating request ID: ${demandeId}...`);
  try {
    const response = await fetch(
      `${BASE_URL}/api/v1/demandes/${demandeId}/validation`,
      {
        method: "PUT",
        headers,
      }
    );
    const validated = await response.json();
    console.log(`✓ Validated! Status: ${validated.statutValidation}`);
  } catch (error) {
    console.error("✗ Failed:", error.message);
  }

  // Test 5: Update payment status
  console.log(`\n[TEST 5] Updating payment status to PAYEE...`);
  try {
    const response = await fetch(
      `${BASE_URL}/api/v1/demandes/${demandeId}/paiement`,
      {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nouveauStatut: "PAYEE" }),
      }
    );

    if (response.ok) {
      console.log("✓ Payment status updated successfully");
    } else {
      console.error("✗ Failed with status:", response.status);
    }
  } catch (error) {
    console.error("✗ Failed:", error.message);
  }

  console.log("\n=== All Tests Completed ===");
}

// Run tests
runTests();
```

---

## 📝 Response Examples

### Successful Create Response (201 Created)

```json
{
  "id": 1,
  "clientId": 42,
  "volume": 25.5,
  "natureMarchandise": "Matériaux de construction",
  "dateDepart": "2025-12-15T08:00:00",
  "adresseDepart": "123 Rue Mohammed V, Casablanca",
  "adresseDestination": "456 Avenue Hassan II, Rabat",
  "statutValidation": "EN_ATTENTE",
  "statutPaiement": "EN_ATTENTE",
  "devisEstime": 1500.0,
  "itineraireAssocieId": null,
  "groupeId": null,
  "dateCreation": "2025-11-26T10:30:00",
  "dateModification": "2025-11-26T10:30:00"
}
```

### Error Response (400 Bad Request)

```json
{
  "timestamp": "2025-11-26T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Le volume doit être positif",
  "path": "/api/v1/demandes"
}
```

### Error Response (401 Unauthorized)

```json
{
  "timestamp": "2025-11-26T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT invalide ou expiré",
  "path": "/api/v1/demandes"
}
```

---

## 🔧 Health Check & Actuator Endpoints

```bash
# Health check
curl http://localhost:8083/actuator/health

# Metrics
curl http://localhost:8083/actuator/metrics

# Info
curl http://localhost:8083/actuator/info
```

---

## 📌 Status Values

### Statut Validation

- `EN_ATTENTE` - Awaiting validation
- `VALIDEE` - Validated by client
- `REFUSEE` - Refused

### Statut Paiement

- `EN_ATTENTE` - Payment pending
- `PAYEE` - Payment completed
- `REMBOURSEE` - Refunded
