package com.ecommerce.auth.config;

// STRANGLER FIG - Phase: 4 - Domain: Authentication
// BC References: BC-053 (audit fields: createdBy, updatedBy populated from security context)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    // BC-053: createdBy/updatedBy populated from JWT principal (email); "system" for unauthenticated ops
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
