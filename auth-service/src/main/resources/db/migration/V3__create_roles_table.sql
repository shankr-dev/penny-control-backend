-- =====================================================
-- Roles Table (Master Role Definitions)
-- Owned by: auth-service
-- Used by: all services
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

-- Create indexes
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_active ON roles(is_active);

-- Insert default roles
INSERT INTO roles (name, description, priority) VALUES
    ('ROLE_USER', 'Regular user with basic permissions', 1),
    ('ROLE_ADMIN', 'Administrator with full permissions', 10);

-- Comments for documentation
COMMENT ON TABLE roles IS 'Master table containing role definitions for RBAC';
COMMENT ON COLUMN roles.id IS 'Primary key';
COMMENT ON COLUMN roles.name IS 'Role name (must start with ROLE_ prefix)';
COMMENT ON COLUMN roles.description IS 'Human-readable description of the role';
COMMENT ON COLUMN roles.priority IS 'Role hierarchy level (higher = more privileged)';
COMMENT ON COLUMN roles.is_active IS 'Whether the role is currently active';
