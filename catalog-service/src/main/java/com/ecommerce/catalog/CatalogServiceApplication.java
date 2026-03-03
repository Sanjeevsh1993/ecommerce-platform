package com.ecommerce.catalog;

// STRANGLER FIG - Phase: 2 - Domain: Catalog
// Migrated from: CatalogManagementFlowCommandExecutor,
//                ecomFlow.xml CatalogManagement + CatalogSearch flows
// BC References: BC-031, BC-032

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
