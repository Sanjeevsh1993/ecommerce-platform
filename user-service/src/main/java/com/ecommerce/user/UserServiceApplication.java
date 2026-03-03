package com.ecommerce.user;

// STRANGLER FIG - Phase: 2 - Domain: Customer / User
// Migrated from: CustomerBO, CustomerDelegate, CustomerSummaryVO,
//                CustomerType, DisplayCustomerSummaryCommand
// BC References: BC-001 through BC-012, BC-053

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
