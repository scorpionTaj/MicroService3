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

                // Configurer les autorisations
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques (pour les tests, actuator, etc.)
                        .requestMatchers("/actuator/**", "/error").permitAll()

                        // Route webhook pour mise à jour paiement (sécurisée différemment)
                        .requestMatchers("/api/v1/demandes/*/paiement").permitAll()

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

