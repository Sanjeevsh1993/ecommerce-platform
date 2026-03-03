package com.ecommerce.order.config;

// BC References: BC-013 (RestClient calls user-service), BC-046 (forward correlation ID)
// Note: Resilience4j TimeLimiter only works with async code (CompletableFuture/reactive).
//       For synchronous RestClient, timeouts must be set on the HTTP request factory directly.

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${app.services.user-service-url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${app.services.user-service-connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${app.services.user-service-read-timeout-ms:10000}")
    private int readTimeoutMs;

    @Bean
    public RestClient userServiceRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(userServiceUrl)
                .requestFactory(factory)
                .requestInterceptor(correlationIdInterceptor())
                .build();
    }

    // BC-046: forward X-Correlation-ID on all outbound calls to user-service
    private ClientHttpRequestInterceptor correlationIdInterceptor() {
        return (request, body, execution) -> {
            String correlationId = CorrelationIdUtils.fromMdc();
            if (correlationId != null && !correlationId.isBlank()) {
                request.getHeaders().set(AppConstants.CORRELATION_ID_HEADER, correlationId);
            }
            return execution.execute(request, body);
        };
    }
}
