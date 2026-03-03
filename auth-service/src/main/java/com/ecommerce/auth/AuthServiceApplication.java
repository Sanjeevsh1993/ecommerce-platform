package com.ecommerce.auth;

// STRANGLER FIG - Phase: 2 - Domain: Authentication
// Migrated from: web.xml security-constraint + security-role definitions
// BC References: BC-034, BC-035, BC-036, BC-037, BC-038, BC-039, BC-040, BC-041, BC-042

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
