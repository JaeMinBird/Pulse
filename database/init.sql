-- CI/CD Dashboard Database Initialization Script
-- Database: cicd_dashboard

-- Create database (run as superuser)
-- CREATE DATABASE cicd_dashboard;
-- \c cicd_dashboard;

-- Create repositories table
CREATE TABLE IF NOT EXISTS repositories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    github_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create builds table
CREATE TABLE IF NOT EXISTS builds (
    id BIGSERIAL PRIMARY KEY,
    repository_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    commit_sha VARCHAR(255) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_repository FOREIGN KEY (repository_id)
        REFERENCES repositories(id) ON DELETE CASCADE
);

-- Create pipelines table (from original schema)
CREATE TABLE IF NOT EXISTS pipelines (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    repository VARCHAR(500) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_builds_repository_id ON builds(repository_id);
CREATE INDEX IF NOT EXISTS idx_builds_status ON builds(status);
CREATE INDEX IF NOT EXISTS idx_builds_commit_sha ON builds(commit_sha);
CREATE INDEX IF NOT EXISTS idx_builds_started_at ON builds(started_at DESC);
CREATE INDEX IF NOT EXISTS idx_repositories_name ON repositories(name);
CREATE INDEX IF NOT EXISTS idx_repositories_created_at ON repositories(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_pipelines_status ON pipelines(status);

-- Insert sample data for repositories
INSERT INTO repositories (name, github_url, created_at) VALUES
    ('backend-api', 'https://github.com/example/backend-api', CURRENT_TIMESTAMP),
    ('frontend-app', 'https://github.com/example/frontend-app', CURRENT_TIMESTAMP),
    ('mobile-app', 'https://github.com/example/mobile-app', CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Insert sample data for builds
INSERT INTO builds (repository_id, status, commit_sha, started_at, completed_at) VALUES
    (1, 'SUCCESS', 'a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour 50 minutes'),
    (1, 'SUCCESS', 'b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '50 minutes'),
    (1, 'IN_PROGRESS', 'c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2', CURRENT_TIMESTAMP - INTERVAL '10 minutes', NULL),
    (2, 'SUCCESS', 'd4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3', CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours 45 minutes'),
    (2, 'FAILED', 'e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4', CURRENT_TIMESTAMP - INTERVAL '30 minutes', CURRENT_TIMESTAMP - INTERVAL '20 minutes'),
    (3, 'SUCCESS', 'f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5', CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours 40 minutes');

-- Insert sample data for pipelines
INSERT INTO pipelines (name, description, repository, branch, status, created_at, updated_at) VALUES
    ('backend-api-ci', 'Continuous integration pipeline for backend API', 'https://github.com/example/backend-api', 'main', 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('frontend-app-deploy', 'Deployment pipeline for frontend application', 'https://github.com/example/frontend-app', 'main', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('mobile-app-test', 'Testing pipeline for mobile application', 'https://github.com/example/mobile-app', 'develop', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Grant permissions (adjust username as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO postgres;
