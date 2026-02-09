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
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs/**", "/favicon.ico").permitAll()
                        .pathMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .pathMatchers("/api/auth/change-password").authenticated()
                        .pathMatchers("/api/auth/admin/**", "/api/auth/admin-reset").hasAuthority("ROLE_ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.POST,"/api/products/search").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/products/**").authenticated()


                        .pathMatchers(HttpMethod.POST, "/api/orders").authenticated()
                        .pathMatchers("/api/orders/my-orders").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/orders/{id}").hasAuthority("ROLE_ADMIN")
                        .pathMatchers("/api/orders/*/confirm", "/api/orders/*/cancel").denyAll()

                        .pathMatchers(HttpMethod.POST, "/api/payments/webhook").permitAll()
                        .pathMatchers("/api/payments/**").authenticated()

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);

        SecretKeySpec spec = new SecretKeySpec(keyBytes, "HmacSHA384");

        return NimbusReactiveJwtDecoder.withSecretKey(spec)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        grantedAuthoritiesConverter.setAuthorityPrefix("");

        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwt -> Flux.fromIterable(grantedAuthoritiesConverter.convert(jwt))
        );

        return jwtAuthenticationConverter;
    }
}