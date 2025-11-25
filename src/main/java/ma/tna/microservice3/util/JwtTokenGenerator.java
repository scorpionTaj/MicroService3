package ma.tna.microservice3.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitaire pour générer des tokens JWT de test
 * Exécuter avec: mvn exec:java -Dexec.mainClass="ma.tna.microservice3.util.JwtTokenGenerator"
 */
public class JwtTokenGenerator {

    // Clé secrète Base64 (doit correspondre à application.properties)
    private static final String SECRET = "eW91ci1zZWNyZXQta2V5LW1pbi0yNTYtYml0cy1sb25nLWNoYW5nZS10aGlzLWluLXByb2R1Y3Rpb24=";

    public static void main(String[] args) {
        System.out.println("=== JWT Token Generator for MicroService3 ===\n");

        // Générer un token pour un client (userId=1)
        String clientToken = generateToken(1L, "CLIENT");
        System.out.println("Token Client (userId=1, role=ROLE_CLIENT):");
        System.out.println(clientToken);
        System.out.println();

        // Générer un token pour un admin (userId=2)
        String adminToken = generateToken(2L, "ADMIN");
        System.out.println("Token Admin (userId=2, role=ROLE_ADMIN):");
        System.out.println(adminToken);
        System.out.println();

        // Générer un token pour un transporteur (userId=3)
        String transporteurToken = generateToken(3L, "TRANSPORTEUR");
        System.out.println("Token Transporteur (userId=3, role=ROLE_TRANSPORTEUR):");
        System.out.println(transporteurToken);
        System.out.println();

        System.out.println("=== Utilisez ces tokens dans Postman ===");
        System.out.println("Ajoutez dans le header: Authorization: Bearer <token>");
    }

    private static String generateToken(Long userId, String role) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        // Token valide pour 365 jours
        long expirationTime = 365L * 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }
}
