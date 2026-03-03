-- BC References: BC-013..025, BC-053

CREATE TABLE IF NOT EXISTS order_history
(
    id                      BIGINT        NOT NULL AUTO_INCREMENT,
    customer_id             BIGINT        NOT NULL,
    customer_number         VARCHAR(50)   NOT NULL,
    order_entry_type_id     INT           NOT NULL,
    order_identifier_type_id INT          NOT NULL,
    order_id                VARCHAR(100)  NOT NULL,
    order_date              DATE          NULL,
    notes                   VARCHAR(2000) NULL,
    external                TINYINT(1)    NOT NULL DEFAULT 0,
    -- BC-016/017: work queue fields — nullable, only set for WQ entries
    work_queue_type_id      INT           NULL,
    -- BC-028: reason IDs are non-sequential (101-103, 201-202) — enforce no range constraint
    work_queue_reason_id    INT           NULL,
    work_queue_notes        VARCHAR(2000) NULL,
    -- BC-053: audit
    created_at              DATETIME(6)   NULL,
    updated_at              DATETIME(6)   NULL,
    created_by              VARCHAR(100)  NULL,
    updated_by              VARCHAR(100)  NULL,
    PRIMARY KEY (id),
    KEY idx_order_history_customer (customer_id),
    KEY idx_order_history_customer_number (customer_number),
    KEY idx_order_history_external (external)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
