package ma.tna.microservice3.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour générer des tokens JWT de test
 * Compatible avec le Service Utilisateurs (Microservice 1)
 * 
 * Exécuter avec: mvn exec:java -Dexec.mainClass="ma.tna.microservice3.util.JwtTokenGenerator"
 * Ou: java -cp target/classes ma.tna.microservice3.util.JwtTokenGenerator
 */
public class JwtTokenGenerator {

    // Clé secrète partagée avec le Service Utilisateurs (texte brut, pas Base64)
    private static final String SECRET = "transport_marchandises_api2025vs2026";

    public static void main(String[] args) {
        System.out.println("=== JWT Token Generator (Compatible Service Utilisateurs) ===\n");
        System.out.println("Secret utilisé: " + SECRET);
        System.out.println("Algorithme: HS256\n");

        // Générer un token pour un client (userId=1)
        String clientToken = generateToken(1L, "client_test@email.com", "CLIENT");
        System.out.println("Token Client (userId=1, role=CLIENT):");
        System.out.println(clientToken);
        System.out.println();

        // Générer un token pour un prestataire (userId=2)
        String prestataireToken = generateToken(2L, "prestataire@email.com", "PRESTATAIRE");
        System.out.println("Token Prestataire (userId=2, role=PRESTATAIRE):");
        System.out.println(prestataireToken);
        System.out.println();

        // Générer un token pour un admin (userId=3)
        String adminToken = generateToken(3L, "admin@email.com", "ADMIN");
        System.out.println("Token Admin (userId=3, role=ADMIN):");
        System.out.println(adminToken);
        System.out.println();

        System.out.println("=== Utilisation dans Postman/cURL ===");
        System.out.println("Header: Authorization: Bearer <token>");
        System.out.println("\nExemple cURL:");
        System.out.println("curl -H \"Authorization: Bearer " + clientToken.substring(0, 50) + "...\" http://localhost:8083/api/v1/demandes");
    }

    /**
     * Génère un token JWT compatible avec le Service Utilisateurs
     */
    private static String generateToken(Long userId, String email, String role) {
        // Clé en texte brut (pas Base64)
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("user_id", userId);  // Alternative claim name
        claims.put("email", email);
        claims.put("role", role);
        claims.put("user_type", role);  // Alternative claim name

        // Token valide pour 24 heures
        long expirationTime = 24L * 60 * 60 * 1000;

        return Jwts.builder()
                .claims(claims)
                .subject(email)  // Le subject est l'email
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }
}
