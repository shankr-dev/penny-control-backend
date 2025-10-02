-- =====================================================
-- Migrate user_roles from VARCHAR to Foreign Key
-- Changes: role (VARCHAR) -> role_id (INT FK to roles table)
-- =====================================================

-- Step 1: Add new role_id column
ALTER TABLE user_roles ADD COLUMN role_id INT;

-- Step 2: Migrate existing data
-- Map string role names to role IDs
UPDATE user_roles SET role_id = (
    SELECT id FROM roles WHERE roles.name = user_roles.role
);

-- Step 3: Drop the old primary key constraint (before dropping the role column)
ALTER TABLE user_roles DROP CONSTRAINT user_roles_pkey;

-- Step 4: Drop the old role column
ALTER TABLE user_roles DROP COLUMN role;

-- Step 5: Make role_id NOT NULL and add foreign key constraint
ALTER TABLE user_roles
    ALTER COLUMN role_id SET NOT NULL,
    ADD CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT;

-- Step 6: Recreate primary key with new column
ALTER TABLE user_roles ADD PRIMARY KEY (user_id, role_id);

-- Step 7: Recreate indexes
DROP INDEX IF EXISTS idx_user_roles_role;
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Comments
COMMENT ON COLUMN user_roles.role_id IS 'Foreign key reference to roles table';
