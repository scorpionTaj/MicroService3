package ma.tna.microservice3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.tna.microservice3.util.JwtUtil;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtre pour l'authentification JWT
 * Intercepte chaque requête pour valider le token JWT
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Exclure les endpoints OpenAPI/Swagger du filtrage JWT
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars") ||
               path.equals("/swagger-ui.html") ||
               path.startsWith("/actuator") ||
               path.equals("/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        logger.debug("Processing request: " + request.getMethod() + " " + request.getRequestURI());
        logger.debug("Authorization header present: " + (authHeader != null));

        // Vérifier si le header Authorization existe et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (authHeader != null) {
                logger.warn("Authorization header does not start with 'Bearer ': " + authHeader.substring(0, Math.min(20, authHeader.length())));
            } else {
                logger.debug("No Authorization header found");
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le token JWT
        jwt = authHeader.substring(7);
        logger.debug("JWT token extracted, length: " + jwt.length());

        try {
            // Extraire le nom d'utilisateur du token
            username = jwtUtil.extractUsername(jwt);
            logger.debug("Extracted username from token: " + username);

            // Si l'utilisateur est valide et qu'il n'y a pas encore d'authentification
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Valider le token
                if (jwtUtil.validateToken(jwt)) {
                    logger.debug("Token is valid");

                    // Extraire les informations du token
                    Long userId = jwtUtil.extractUserId(jwt);
                    String role = jwtUtil.extractRole(jwt);
                    logger.info("JWT Authentication successful - userId: " + userId + ", role: " + role);

                    // Créer l'authentification
                    // Ajouter le préfixe ROLE_ si absent
                    String roleWithPrefix = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId,  // Le principal est l'ID utilisateur
                            null,
                            Collections.singletonList(authority)
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

