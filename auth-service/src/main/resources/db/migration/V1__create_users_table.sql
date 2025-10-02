-- =====================================================
-- Users Table (Core Authentication & Profile)
-- Owned by: auth-service
-- Used by: all services
-- =====================================================

CREATE TABLE users (
    -- Core Identity
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,

    -- Profile Information
    name            VARCHAR(100),
    phone_number    VARCHAR(20) UNIQUE,
    currency        CHAR(3) DEFAULT 'USD',

    -- Account Status
    email_verified  BOOLEAN DEFAULT FALSE,
    account_locked  BOOLEAN DEFAULT FALSE,
    enabled         BOOLEAN DEFAULT TRUE,

    -- Audit Fields
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number) WHERE phone_number IS NOT NULL;
CREATE INDEX idx_users_enabled ON users(enabled);

-- Comments for documentation
COMMENT ON TABLE users IS 'Core users table containing authentication and profile information';
COMMENT ON COLUMN users.email IS 'Unique email address used for login';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hashed password';
COMMENT ON COLUMN users.email_verified IS 'Whether the email has been verified';
COMMENT ON COLUMN users.account_locked IS 'Whether the account is locked (security)';
COMMENT ON COLUMN users.enabled IS 'Whether the account is active';
