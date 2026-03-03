-- Fix: change customer_type from VARCHAR to ENUM to match Hibernate 6 mapping of @Enumerated(EnumType.STRING)
-- BC References: BC-005 (CustomerType enum)

ALTER TABLE customers
    MODIFY customer_type ENUM('B2C','B2B','RESELLER','WHOLESALE') NOT NULL DEFAULT 'B2C';
