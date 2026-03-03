-- BC References: BC-001..012, BC-053 (audit fields)

CREATE TABLE IF NOT EXISTS customers
(
    id                            BIGINT        NOT NULL AUTO_INCREMENT,
    customer_number               VARCHAR(50)   NOT NULL,
    first_name                    VARCHAR(100)  NOT NULL,
    last_name                     VARCHAR(100)  NOT NULL,
    email                         VARCHAR(255)  NOT NULL,
    phone                         VARCHAR(30)   NULL,
    customer_type                 VARCHAR(20)   NOT NULL DEFAULT 'B2C',
    prospect                      TINYINT(1)    NOT NULL DEFAULT 0,
    special_assistance_indicators VARCHAR(500)  NULL,
    recommendation_engine_url     VARCHAR(500)  NULL,
    crm_url                       VARCHAR(500)  NULL,
    test_customer_indicator       VARCHAR(1)    NULL,
    created_at                    DATETIME(6)   NULL,
    updated_at                    DATETIME(6)   NULL,
    created_by                    VARCHAR(100)  NULL,
    updated_by                    VARCHAR(100)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_customers_email (email),
    UNIQUE KEY uq_customers_number (customer_number),
    KEY idx_customers_type (customer_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
