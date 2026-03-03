package com.ecommerce.order.config;

// BC References: BC-013 (WebClient calls user-service), BC-046 (forward correlation ID)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Value("${app.services.user-service-url:http://localhost:8082}")
    private String userServiceUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .filter(correlationIdFilter())
                .build();
    }

    // BC-046: forward X-Correlation-ID on all outbound calls to user-service
    private ExchangeFilterFunction correlationIdFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String correlationId = CorrelationIdUtils.fromMdc();
            if (correlationId != null && !correlationId.isBlank()) {
                return Mono.just(ClientRequest.from(request)
                        .header(AppConstants.CORRELATION_ID_HEADER, correlationId)
                        .build());
            }
            return Mono.just(request);
        });
    }
}
