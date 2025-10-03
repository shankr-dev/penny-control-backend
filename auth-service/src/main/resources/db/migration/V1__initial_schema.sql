-- =====================================================
-- Initial Database Schema for Penny Control Backend
-- Version: 1.0
-- Created: 2025-10-02
-- =====================================================

-- =====================================================
-- 1. USERS TABLE (Core Authentication & Profile)
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

-- =====================================================
-- 2. ROLES TABLE (Master Role Definitions)
-- =====================================================
CREATE TABLE roles (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    priority    INT DEFAULT 0,
    is_active   BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_role_name_format CHECK (name ~ '^ROLE_[A-Z_]+$')
);

-- Indexes
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_active ON roles(is_active);

-- Seed default roles
INSERT INTO roles (name, description, priority) VALUES
    ('ROLE_USER', 'Regular user with basic permissions', 1),
    ('ROLE_ADMIN', 'Administrator with full permissions', 10);

-- Comments
COMMENT ON TABLE roles IS 'Master table containing role definitions for RBAC';
COMMENT ON COLUMN roles.name IS 'Role name (must start with ROLE_ prefix)';
COMMENT ON COLUMN roles.priority IS 'Role hierarchy level (higher = more privileged)';

-- =====================================================
-- 3. USER_ROLES TABLE (Many-to-Many Join Table)
-- =====================================================
CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role_id     INT NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- Indexes
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Comments
COMMENT ON TABLE user_roles IS 'Join table mapping users to their assigned roles';
COMMENT ON COLUMN user_roles.user_id IS 'Reference to users table';
COMMENT ON COLUMN user_roles.role_id IS 'Reference to roles table';

-- =====================================================
-- 4. REFRESH_TOKENS TABLE (Session Management)
-- =====================================================
CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    token_hash      VARCHAR(255) UNIQUE NOT NULL,

    -- Token lifecycle
    issued_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL,
    revoked_at      TIMESTAMP,
    is_revoked      BOOLEAN DEFAULT FALSE,

    -- Session tracking
    ip_address      VARCHAR(45),
    user_agent      TEXT,

    -- Security & audit
    last_used_at    TIMESTAMP,
    usage_count     INT DEFAULT 0,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens(is_revoked, expires_at);

-- Comments
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for session management and logout functionality';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of the refresh token';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'Whether the token has been revoked (logout)';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP address where token was issued';
COMMENT ON COLUMN refresh_tokens.user_agent IS 'Browser/app user agent';
COMMENT ON COLUMN refresh_tokens.last_used_at IS 'Last time token was used for refresh';
COMMENT ON COLUMN refresh_tokens.usage_count IS 'Number of times token was used (for rotation detection)';
