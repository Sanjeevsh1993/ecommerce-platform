package com.ecommerce.gateway.filter;

// BC References: BC-046 (X-Correlation-ID propagated), BC-047 (MDC correlation ID)

import com.ecommerce.shared.constants.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void filter_existingCorrelationId_propagatesIt() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/customers/1")
                .header(AppConstants.CORRELATION_ID_HEADER, "my-corr-id")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // BC-046: response must echo back the same correlation ID
        assertThat(exchange.getResponse().getHeaders()
                .getFirst(AppConstants.CORRELATION_ID_HEADER)).isEqualTo("my-corr-id");
    }

    @Test
    void filter_noCorrelationId_generatesNewOne() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/catalog/items/1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(AppConstants.CORRELATION_ID_HEADER);
        // BC-046: a new UUID-based correlation ID must be generated
        assertThat(responseCorrelationId).isNotBlank();
        assertThat(responseCorrelationId).matches("[0-9a-f-]{36}");
    }

    @Test
    void filter_hasHighestPrecedenceOrder() {
        assertThat(filter.getOrder()).isEqualTo(Integer.MIN_VALUE);
    }
}
