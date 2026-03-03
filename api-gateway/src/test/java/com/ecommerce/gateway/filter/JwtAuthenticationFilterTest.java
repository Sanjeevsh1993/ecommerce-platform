package com.ecommerce.gateway.filter;

// BC References: BC-035 (token validation), BC-040 (401 on missing/invalid token),
//                BC-042 (email + roles injected as downstream headers)

import com.ecommerce.gateway.security.JwtValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtValidator jwtValidator;

    @InjectMocks JwtAuthenticationFilter filter;

    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(filter, "publicPaths", List.of(
                "/api/v1/auth/**", "/actuator/health", "/swagger-ui/**",
                "/swagger/**", "/fallback/**", "/v3/api-docs/**"));
        chain = mock(GatewayFilterChain.class);
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_publicPath_skipsJwtValidation() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // No JWT validator called — chain proceeds
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void filter_missingAuthHeader_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/customers/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_invalidToken_returns401() {
        when(jwtValidator.isValid("bad-token")).thenReturn(false);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/customers/1")
                .header("Authorization", "Bearer bad-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filter_validToken_propagatesEmailAndRolesHeaders() {
        when(jwtValidator.isValid("good-token")).thenReturn(true);
        when(jwtValidator.extractEmail("good-token")).thenReturn("user@example.com");
        when(jwtValidator.extractRoles("good-token")).thenReturn(List.of("ROLE_CUSTOMER"));

        // Capture mutated exchange via chain
        final String[] capturedEmail = new String[1];
        final String[] capturedRoles = new String[1];
        GatewayFilterChain capturingChain = exchange -> {
            capturedEmail[0] = exchange.getRequest().getHeaders().getFirst("X-User-Email");
            capturedRoles[0] = exchange.getRequest().getHeaders().getFirst("X-User-Roles");
            return Mono.empty();
        };

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/customers/1")
                .header("Authorization", "Bearer good-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, capturingChain))
                .verifyComplete();

        // BC-042: downstream gets the extracted claims
        assertThat(capturedEmail[0]).isEqualTo("user@example.com");
        assertThat(capturedRoles[0]).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    void filter_actuatorHealth_isPublicPath() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void filter_hasOrderHighestPrecedencePlusOne() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE + 1);
    }
}
