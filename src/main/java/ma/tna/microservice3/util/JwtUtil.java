package ma.tna.microservice3.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Utilitaire pour la gestion et validation des tokens JWT
 * Compatible avec le Service Utilisateurs (Microservice 1)
 * Algorithme: HS256
 * Secret: Clé partagée en texte brut
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Génère la clé de signature à partir du secret (texte brut, pas Base64)
     * Compatible avec le Service Utilisateurs qui utilise: transport_marchandises_api2025vs2026
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrait tous les claims du token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrait un claim spécifique du token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait le nom d'utilisateur (subject) du token
     * Le Service Utilisateurs peut utiliser 'sub', 'username', ou 'email'
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        // Essayer d'abord le subject standard
        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            // Fallback sur le claim 'username' si présent
            username = (String) claims.get("username");
        }
        if (username == null || username.isBlank()) {
            // Fallback sur le claim 'email' (utilisé par Service Utilisateurs)
            username = (String) claims.get("email");
        }
        if (username == null || username.isBlank()) {
            // Fallback sur user_id comme string
            Object userId = claims.get("user_id");
            if (userId != null) {
                username = userId.toString();
            }
        }
        logger.debug("extractUsername - sub: {}, username: {}, email: {}, result: {}", 
                claims.getSubject(), claims.get("username"), claims.get("email"), username);
        return username;
    }

    /**
     * Extrait l'ID utilisateur du token
     * Compatible avec différents formats: userId, user_id, id
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        
        // Essayer différents noms de claims possibles
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            userIdObj = claims.get("user_id");
        }
        if (userIdObj == null) {
            userIdObj = claims.get("id");
        }
        
        // Convertir en Long
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                logger.warn("Impossible de parser userId: {}", userIdObj);
            }
        }
        return null;
    }

    /**
     * Extrait le rôle de l'utilisateur du token
     * Compatible avec: role, user_type, type (CLIENT, PRESTATAIRE, ADMIN)
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        
        // Essayer différents noms de claims pour le rôle
        String role = (String) claims.get("role");
        if (role == null || role.isBlank()) {
            role = (String) claims.get("user_type");
        }
        if (role == null || role.isBlank()) {
            role = (String) claims.get("type");
        }
        
        return role;
    }

    /**
     * Extrait l'email de l'utilisateur (si présent dans le token)
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("email");
    }

    /**
     * Extrait la date d'expiration du token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Vérifie si le token est expiré
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valide le token
     */
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token); // Vérifie la signature
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Erreur de validation du token JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Affiche les claims du token pour le debug
     */
    public void logTokenClaims(String token) {
        try {
            Claims claims = extractAllClaims(token);
            logger.debug("JWT Claims: {}", claims);
        } catch (Exception e) {
            logger.error("Impossible de parser le token: {}", e.getMessage());
        }
    }
}

