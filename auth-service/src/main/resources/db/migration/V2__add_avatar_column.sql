-- =====================================================
-- Add avatar column to users table
-- Version: 2.0
-- Created: 2025-10-03
-- =====================================================

-- Add avatar column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS avatar VARCHAR(500);

-- Add comment for documentation
COMMENT ON COLUMN users.avatar IS 'URL or path to user profile picture/avatar';
