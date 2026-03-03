package com.ecommerce.order.config;

// BC References: BC-013 (RestClient calls user-service), BC-046 (forward correlation ID)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.services.user-service-url:http://localhost:8082}")
    private String userServiceUrl;

    @Bean
    public RestClient userServiceRestClient() {
        return RestClient.builder()
                .baseUrl(userServiceUrl)
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
