-- BC References: BC-031, BC-032, BC-053

CREATE TABLE IF NOT EXISTS catalog_items
(
    id             BIGINT           NOT NULL AUTO_INCREMENT,
    item_code      VARCHAR(100)     NOT NULL,
    name           VARCHAR(255)     NOT NULL,
    description    VARCHAR(2000)    NULL,
    price          DECIMAL(10, 2)   NULL,
    category       VARCHAR(100)     NULL,
    active         TINYINT(1)       NOT NULL DEFAULT 1,
    stock_quantity INT              NULL,
    created_at     DATETIME(6)      NULL,
    updated_at     DATETIME(6)      NULL,
    created_by     VARCHAR(100)     NULL,
    updated_by     VARCHAR(100)     NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_catalog_items_code (item_code),
    KEY idx_catalog_items_active (active),
    KEY idx_catalog_items_category (category)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
