-- =====================================================
-- Single Database for All Microservices
-- Follows "Shared Database" pattern for monolith-first approach
-- Each service owns specific tables within this database
-- =====================================================

-- Create the main application database
-- This script runs automatically when Docker container starts
-- Note: PostgreSQL will also create this as default DB via POSTGRES_DB env var

CREATE DATABASE IF NOT EXISTS penny_control_db;

-- Grant all privileges to postgres user (default)
GRANT ALL PRIVILEGES ON DATABASE penny_control_db TO postgres;

-- Future: Add read-only user for reporting/analytics
-- CREATE USER penny_readonly WITH PASSWORD 'readonly_password';
-- GRANT CONNECT ON DATABASE penny_control_db TO penny_readonly;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO penny_readonly;