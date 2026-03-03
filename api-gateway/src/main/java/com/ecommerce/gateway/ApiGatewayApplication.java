package com.ecommerce.gateway;

// STRANGLER FIG - Phase: 2 - Domain: Gateway / Cross-Cutting
// Migrated from: ControllerServlet (/control endpoint dispatcher)
// BC References: BC-035, BC-046, BC-047, BC-048, BC-049, BC-050

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

// Gateway is reactive (WebFlux) — exclude JPA/DataSource auto-config
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
