-- BC References: BC-034 (user roles), BC-035 (password hash), BC-036 (account state),
--                BC-038 (refresh tokens), BC-039 (7-day token expiry), BC-053 (audit fields)
-- Flyway migration V1 — ecommerce_auth schema bootstrap

CREATE TABLE IF NOT EXISTS users
(
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100)    NOT NULL,
    email                       VARCHAR(255)    NOT NULL,
    password_hash               VARCHAR(255)    NOT NULL,
    enabled                     TINYINT(1)      NOT NULL DEFAULT 1,
    account_non_locked          TINYINT(1)      NOT NULL DEFAULT 1,
    password_reset_token        VARCHAR(255)    NULL,
    password_reset_token_expiry DATETIME(6)     NULL,
    -- BC-053: audit columns from BaseEntity
    created_at                  DATETIME(6)     NULL,
    updated_at                  DATETIME(6)     NULL,
    created_by                  VARCHAR(100)    NULL,
    updated_by                  VARCHAR(100)    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    KEY idx_users_password_reset_token (password_reset_token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- BC-034: roles stored as a join table (ElementCollection)
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- BC-038: refresh tokens stored as SHA-256 hashes; raw token only in HTTP-only cookie
-- BC-039: 7-day expiry enforced via expiresAt column
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    token_hash VARCHAR(512) NOT NULL,
    user_id    BIGINT       NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    revoked    TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_refresh_tokens_hash (token_hash),
    KEY idx_refresh_tokens_user (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
