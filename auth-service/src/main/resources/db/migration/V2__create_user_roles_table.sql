-- =====================================================
-- User Roles Table (Role-Based Access Control)
-- Owned by: auth-service
-- Used by: all services
-- =====================================================

CREATE TABLE user_roles (
    user_id     BIGINT NOT NULL,
    role        VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- Comments for documentation
COMMENT ON TABLE user_roles IS 'User role assignments for RBAC (Role-Based Access Control)';
COMMENT ON COLUMN user_roles.user_id IS 'Reference to users table';
COMMENT ON COLUMN user_roles.role IS 'Role name (e.g., ROLE_USER, ROLE_ADMIN)';
COMMENT ON COLUMN user_roles.assigned_at IS 'When the role was assigned';
