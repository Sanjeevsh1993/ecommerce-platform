package com.ecommerce.gateway.config;

// STRANGLER FIG - Phase: 5 - Domain: Gateway / Cross-Cutting
// IMPORTANT: This is WebFlux security (ServerHttpSecurity), NOT Servlet HttpSecurity.
// BC References: BC-042 (stateless), BC-048 (CORS via globalcors in yml)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Spring Security is intentionally permissive here.
     * JWT authentication and authorisation are enforced by the custom
     * {@link com.ecommerce.gateway.filter.JwtAuthenticationFilter} GlobalFilter,
     * which runs before routing and returns 401 for invalid/missing tokens.
     *
     * CORS is configured via spring.cloud.gateway.globalcors in application.yml (BC-048).
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // BC-042: stateless — no sessions
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                // Permit all at Spring Security level — JwtAuthenticationFilter enforces auth
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}
