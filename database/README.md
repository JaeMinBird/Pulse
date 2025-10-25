# Database Setup Guide

## PostgreSQL Database Configuration

This guide will help you set up the PostgreSQL database for the CI/CD Pipeline Dashboard.

## Prerequisites

- PostgreSQL 12 or higher installed
- PostgreSQL client (psql) or GUI tool (pgAdmin, DBeaver)

## Database Setup

### Option 1: Using psql Command Line

1. **Connect to PostgreSQL as superuser:**
```bash
psql -U postgres
```

2. **Create the database:**
```sql
CREATE DATABASE cicd_dashboard;
```

3. **Create a user (optional but recommended):**
```sql
CREATE USER cicd_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE cicd_dashboard TO cicd_user;
```

4. **Connect to the database and run the initialization script:**
```bash
psql -U postgres -d cicd_dashboard -f init.sql
```

### Option 2: Automatic with Spring Boot

Spring Boot will automatically create the tables when you run the application with `spring.jpa.hibernate.ddl-auto=update` (default configuration).

Simply ensure PostgreSQL is running and the database exists:
```bash
psql -U postgres -c "CREATE DATABASE cicd_dashboard;"
```

Then start the Spring Boot application, and Hibernate will create the tables automatically.

### Option 3: Using Docker

Run PostgreSQL in a Docker container:

```bash
docker run --name cicd-postgres \
  -e POSTGRES_DB=cicd_dashboard \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15
```

Then run the initialization script:
```bash
docker exec -i cicd-postgres psql -U postgres -d cicd_dashboard < init.sql
```

## Database Schema

### Tables

#### 1. repositories
Stores information about Git repositories being monitored.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| name | VARCHAR(255) | Repository name (unique) |
| github_url | VARCHAR(500) | GitHub repository URL |
| created_at | TIMESTAMP | Creation timestamp |

#### 2. builds
Stores CI/CD build information for each repository.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| repository_id | BIGINT | Foreign key to repositories |
| status | VARCHAR(50) | Build status (PENDING, IN_PROGRESS, SUCCESS, FAILED, CANCELLED) |
| commit_sha | VARCHAR(255) | Git commit SHA |
| started_at | TIMESTAMP | Build start time |
| completed_at | TIMESTAMP | Build completion time (nullable) |

#### 3. pipelines
Stores pipeline configuration and status.

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| name | VARCHAR(255) | Pipeline name |
| description | VARCHAR(1000) | Pipeline description |
| repository | VARCHAR(500) | Repository URL |
| branch | VARCHAR(255) | Git branch |
| status | VARCHAR(50) | Pipeline status |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

## Connection Configuration

Update the connection details in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cicd_dashboard
    username: postgres
    password: postgres
```

Or use environment variables:
```bash
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

## Verification

After setup, verify the tables were created:

```sql
\c cicd_dashboard
\dt
```

You should see:
- repositories
- builds
- pipelines

## Sample Data

The `init.sql` script includes sample data:
- 3 repositories (backend-api, frontend-app, mobile-app)
- 6 builds with various statuses
- 3 pipelines

To skip sample data, comment out the INSERT statements in `init.sql`.

## Troubleshooting

### Connection refused
- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Check if port 5432 is open: `netstat -an | grep 5432`

### Authentication failed
- Verify credentials in application.yml
- Check PostgreSQL pg_hba.conf for authentication settings

### Database does not exist
```bash
createdb -U postgres cicd_dashboard
```

### Permission denied
```sql
GRANT ALL PRIVILEGES ON DATABASE cicd_dashboard TO your_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
```

## Useful Commands

```sql
-- View all repositories
SELECT * FROM repositories;

-- View builds for a specific repository
SELECT b.*, r.name as repo_name
FROM builds b
JOIN repositories r ON b.repository_id = r.id
WHERE r.name = 'backend-api';

-- Count builds by status
SELECT status, COUNT(*)
FROM builds
GROUP BY status;

-- View recent builds
SELECT b.id, r.name, b.status, b.commit_sha, b.started_at
FROM builds b
JOIN repositories r ON b.repository_id = r.id
ORDER BY b.started_at DESC
LIMIT 10;
```
