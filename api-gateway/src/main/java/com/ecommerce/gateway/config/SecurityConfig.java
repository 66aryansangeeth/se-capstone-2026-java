package com.ecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // === 1. AUTH SERVICE ROUTES ===
                        .pathMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .pathMatchers("/api/auth/change-password").authenticated()
                        .pathMatchers("/api/auth/admin/**", "/api/auth/admin-reset").hasAuthority("ROLE_ADMIN")

                        // === 2. PRODUCT SERVICE ROUTES ===
                        // Public: Browsing
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        // Admin Only: Mutations
                        .pathMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        // Internal/Auth Required: Reduce Stock (called during order placement)
                        .pathMatchers(HttpMethod.PATCH, "/api/products/**").authenticated()

                        // === 3. ORDER SERVICE ROUTES ===
                        // User Level: Place orders and see their own history
                        .pathMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .pathMatchers("/api/orders/my-orders").authenticated()
                        // Admin Level: View any specific order by ID
                        .pathMatchers(HttpMethod.GET, "/api/orders/{id}").hasAuthority("ROLE_ADMIN")
                        // Internal Level: Block External access to confirmation (used by Payment Service)
                        .pathMatchers("/api/orders/*/confirm").denyAll()

                        // === 4. CATCH-ALL ===
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // 1. MUST decode using Base64 to match Auth Service's 'Decoders.BASE64.decode'
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);

        // 2. Create the SecretKeySpec for HMAC SHA-256
        SecretKeySpec spec = new SecretKeySpec(keyBytes, "HmacSHA384");

        // 3. Explicitly set the algorithm to HS256 to match Jwts.builder() default
        return NimbusReactiveJwtDecoder.withSecretKey(spec)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Set to "roles" or "scope" depending on your JWT.io payload
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        // Set to empty string because your token already has "ROLE_" prefix
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

        // Instead of the "Enumerable" adapter, use this Lambda:
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwt -> Flux.fromIterable(grantedAuthoritiesConverter.convert(jwt))
        );

        return jwtAuthenticationConverter;
    }
}