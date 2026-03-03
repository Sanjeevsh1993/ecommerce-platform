package com.ecommerce.order.config;

// BC References: BC-013 (Feign calls user-service), BC-046 (forward correlation ID)

import com.ecommerce.shared.constants.AppConstants;
import com.ecommerce.shared.util.CorrelationIdUtils;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    // BC-046: forward X-Correlation-ID on all Feign calls to user-service
    @Bean
    public RequestInterceptor correlationIdFeignInterceptor() {
        return template -> template.header(
                AppConstants.CORRELATION_ID_HEADER, CorrelationIdUtils.fromMdc());
    }
}
