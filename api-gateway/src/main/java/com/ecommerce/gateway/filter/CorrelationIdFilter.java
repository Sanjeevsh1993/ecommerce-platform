package com.ecommerce.gateway.filter;

// STRANGLER FIG - Phase: 5 - Domain: Gateway / Cross-Cutting
// BC References: BC-046 (X-Correlation-ID propagated on every request and response),
//                BC-047 (correlationId in MDC log pattern)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    // Run first — before JWT filter and before routing
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incoming = exchange.getRequest().getHeaders()
                .getFirst(AppConstants.CORRELATION_ID_HEADER);
        String correlationId = CorrelationIdUtils.resolveOrGenerate(incoming);

        // BC-046: add X-Correlation-ID to forwarded request
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(AppConstants.CORRELATION_ID_HEADER, correlationId)
                .build();

        // BC-046: add X-Correlation-ID to outgoing response
        exchange.getResponse().getHeaders()
                .add(AppConstants.CORRELATION_ID_HEADER, correlationId);

        // BC-047: set MDC for the reactive context (best-effort on single thread)
        CorrelationIdUtils.setMdc(correlationId);

        log.debug("Request [{}] {} {}", correlationId,
                exchange.getRequest().getMethod(), exchange.getRequest().getPath());

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> CorrelationIdUtils.clearMdc());
    }
}
