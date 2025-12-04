package ma.tna.microservice3.config;

import ma.tna.microservice3.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security 6+ avec JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF car on utilise JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Autoriser les frames pour la console H2
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))

                // Configurer les autorisations
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (pour les tests, actuator, etc.)
                        .requestMatchers("/actuator/**", "/error", "/h2-console/**").permitAll()

                        // Route webhook pour mise à jour paiement (sécurisée différemment)
                        .requestMatchers("/api/v1/demandes/*/paiement").permitAll()

                        // Routes catégories publiques (lecture seule)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/categories/**").permitAll()

                        // Swagger & OpenAPI routes (sans authentification)
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // Routes catégories modification (authentification requise)
                        .requestMatchers("/api/v1/categories/**").authenticated()

                        // Toutes les autres routes nécessitent une authentification
                        .requestMatchers("/api/v1/demandes/**").authenticated()

                        .anyRequest().authenticated()
                )

                // Configurer la gestion de session (stateless pour JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Ajouter le filtre JWT avant le filtre d'authentification standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

