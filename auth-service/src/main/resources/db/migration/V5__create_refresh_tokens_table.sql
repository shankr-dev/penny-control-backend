-- =====================================================
-- Refresh Tokens Table (Session Management)
-- Owned by: auth-service
-- Used by: auth-service only
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
    device_id       VARCHAR(255),

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

-- Comments for documentation
COMMENT ON TABLE refresh_tokens IS 'Stores refresh tokens for session management and logout functionality';
COMMENT ON COLUMN refresh_tokens.token_hash IS 'SHA-256 hash of the refresh token';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'Whether the token has been revoked (logout)';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP address where token was issued';
COMMENT ON COLUMN refresh_tokens.user_agent IS 'Browser/app user agent';
COMMENT ON COLUMN refresh_tokens.device_id IS 'Optional client-generated device identifier';
COMMENT ON COLUMN refresh_tokens.last_used_at IS 'Last time token was used for refresh';
COMMENT ON COLUMN refresh_tokens.usage_count IS 'Number of times token was used (for rotation detection)';
