-- Pre-create all service databases
-- Each microservice gets its own schema (separate concern per BC-053)
-- Flyway runs per-service migrations automatically on startup.

CREATE DATABASE IF NOT EXISTS ecommerce_auth     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_users    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_orders   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ecommerce_catalog  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant all on all service DBs to root (already has it) and ecommerce user
GRANT ALL PRIVILEGES ON ecommerce_auth.*    TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_users.*   TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_orders.*  TO 'ecommerce'@'%';
GRANT ALL PRIVILEGES ON ecommerce_catalog.* TO 'ecommerce'@'%';
FLUSH PRIVILEGES;
