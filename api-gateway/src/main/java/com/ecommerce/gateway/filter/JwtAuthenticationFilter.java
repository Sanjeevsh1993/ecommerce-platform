package com.ecommerce.gateway.filter;

// STRANGLER FIG - Phase: 5 - Domain: Gateway / Cross-Cutting
// Migrated from: web.xml <security-constraint> URL pattern protection
// BC References: BC-034 (role-based access), BC-035 (token validation),
//                BC-040 (validate Bearer token before routing),
//                BC-042 (propagate email + roles as downstream headers)

import com.ecommerce.gateway.security.JwtValidator;
import com.ecommerce.shared.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtValidator jwtValidator;

    // Paths that do NOT require a JWT (configured in application.yml, comma-separated)
    @Value("#{'${app.gateway.public-paths}'.split(',')}")
    private List<String> publicPaths;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Run after CorrelationIdFilter (HIGHEST_PRECEDENCE + 1)
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // BC-040: skip JWT check for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);
        if (!jwtValidator.isValid(token)) {
            return unauthorized(exchange, "JWT token is invalid or expired");
        }

        // BC-042: propagate extracted claims as headers for downstream services
        String email = jwtValidator.extractEmail(token);
        List<String> roles = jwtValidator.extractRoles(token);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Email", email)
                .header("X-User-Roles", String.join(",", roles))
                .build();

        log.debug("JWT validated — email={} path={}", email, path);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        String correlationId = exchange.getRequest().getHeaders()
                .getFirst(AppConstants.CORRELATION_ID_HEADER);
        log.warn("Unauthorized request — correlationId={} reason={}", correlationId, reason);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"%s\"},\"correlationId\":\"%s\"}",
                reason, correlationId != null ? correlationId : "unknown");

        var buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
