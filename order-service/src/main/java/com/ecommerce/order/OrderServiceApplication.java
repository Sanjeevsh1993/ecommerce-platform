package com.ecommerce.order;

// STRANGLER FIG - Phase: 2 - Domain: Order / Work Queue
// Migrated from: OrderHistoryDelegate, OrderHistoryVO, ViewOrderHistoryVO,
//                OrderHistoryAddCommand, OrderHistorySaveAddCommand,
//                OrderIdentifierType, OrderHistoryTag,
//                ecomFlow.xml actions 71-76, 90-91
// BC References: BC-013 through BC-029, BC-033, BC-054, BC-055, BC-056

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
