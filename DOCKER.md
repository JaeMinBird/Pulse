# Docker Deployment Guide

This guide explains how to run the CI/CD Pipeline Dashboard using Docker and Docker Compose.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Docker Commands](#docker-commands)
- [Architecture](#architecture)
- [Environment Variables](#environment-variables)
- [Volumes and Persistence](#volumes-and-persistence)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)
- [Production Deployment](#production-deployment)

## Prerequisites

- Docker 20.10 or higher
- Docker Compose 2.0 or higher
- 2GB RAM minimum (4GB recommended)
- 5GB disk space

### Install Docker

**Windows/Mac:**
- Download [Docker Desktop](https://www.docker.com/products/docker-desktop)

**Linux:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**Verify installation:**
```bash
docker --version
docker-compose --version
```

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd "Peraton Proj"
```

### 2. Set Environment Variables

Copy the example environment file:
```bash
cp .env.example .env
```

Edit `.env` and add your GitHub token:
```bash
GITHUB_TOKEN=ghp_your_github_personal_access_token_here
DB_PASSWORD=your_secure_password
```

### 3. Start the Application

```bash
docker-compose up -d
```

This will:
- Build the Spring Boot application
- Start PostgreSQL database
- Start the application
- Initialize the database schema

### 4. Verify Deployment

Check if containers are running:
```bash
docker-compose ps
```

Check application logs:
```bash
docker-compose logs -f app
```

Access the application:
```
http://localhost:8080/api/builds
```

### 5. Stop the Application

```bash
docker-compose down
```

To also remove volumes (database data):
```bash
docker-compose down -v
```

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Database
DB_PASSWORD=postgres

# GitHub API
GITHUB_TOKEN=ghp_your_token_here

# Scheduler
SCHEDULER_ENABLED=true
SCHEDULER_GITHUB_SYNC_RATE=300000

# Logging
LOG_LEVEL=INFO

# Spring Profile
SPRING_PROFILE=docker
```

### Docker Compose Configuration

The `docker-compose.yml` includes two services:

#### PostgreSQL Service

```yaml
postgres:
  image: postgres:15-alpine
  ports:
    - "5432:5432"
  environment:
    POSTGRES_DB: cicd_dashboard
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: ${DB_PASSWORD}
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
```

#### Application Service

```yaml
app:
  build:
    context: .
    dockerfile: Dockerfile
  ports:
    - "8080:8080"
  depends_on:
    postgres:
      condition: service_healthy
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cicd_dashboard
    GITHUB_TOKEN: ${GITHUB_TOKEN}
```

## Docker Commands

### Build and Start

```bash
# Build and start all services
docker-compose up -d

# Build without cache
docker-compose build --no-cache

# Start only specific service
docker-compose up -d postgres
```

### Manage Containers

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f app

# Restart service
docker-compose restart app

# Stop all services
docker-compose stop

# Start existing services
docker-compose start
```

### Clean Up

```bash
# Stop and remove containers
docker-compose down

# Stop, remove containers and volumes
docker-compose down -v

# Remove all (containers, volumes, images)
docker-compose down -v --rmi all

# Prune Docker system
docker system prune -a --volumes
```

### Access Container Shell

```bash
# Access application container
docker-compose exec app sh

# Access database container
docker-compose exec postgres psql -U postgres -d cicd_dashboard

# Run database queries
docker-compose exec postgres psql -U postgres -d cicd_dashboard -c "SELECT * FROM repositories;"
```

## Architecture

### Multi-Stage Dockerfile

The Dockerfile uses a multi-stage build:

**Stage 1: Build**
- Uses Maven image with JDK 17
- Downloads dependencies
- Compiles and packages the application

**Stage 2: Runtime**
- Uses JRE 17 (smaller image)
- Copies only the JAR file
- Runs as non-root user for security

### Network Architecture

```
┌─────────────────────────────────────┐
│         Docker Network              │
│                                     │
│  ┌──────────────┐  ┌─────────────┐│
│  │   App        │  │  PostgreSQL ││
│  │   :8080      │──│  :5432      ││
│  └──────────────┘  └─────────────┘│
│         │                          │
└─────────┼──────────────────────────┘
          │
     Host :8080
```

### Service Dependencies

```
app:
  depends_on:
    postgres:
      condition: service_healthy
```

The application waits for PostgreSQL to be healthy before starting.

## Environment Variables

### Complete List

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_PASSWORD` | postgres | PostgreSQL password |
| `GITHUB_TOKEN` | - | GitHub personal access token |
| `SCHEDULER_ENABLED` | true | Enable scheduled sync |
| `SCHEDULER_GITHUB_SYNC_RATE` | 300000 | Sync interval (ms) |
| `LOG_LEVEL` | INFO | Application log level |
| `SPRING_PROFILE` | docker | Spring profile to use |
| `SERVER_PORT` | 8080 | Application port |
| `GITHUB_API_BASE_URL` | https://api.github.com | GitHub API URL |
| `GITHUB_API_TIMEOUT` | 10000 | API timeout (ms) |

### Override in Docker Compose

```bash
# Override environment variable
GITHUB_TOKEN=new_token docker-compose up -d

# Use different compose file
docker-compose -f docker-compose.prod.yml up -d
```

## Volumes and Persistence

### Named Volumes

```yaml
volumes:
  postgres_data:
    driver: local
```

**Location:**
- Linux: `/var/lib/docker/volumes/`
- Windows: `\\wsl$\docker-desktop-data\data\docker\volumes\`
- Mac: `~/Library/Containers/com.docker.docker/Data/`

### Volume Management

```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect peraton-proj_postgres_data

# Backup database
docker-compose exec postgres pg_dump -U postgres cicd_dashboard > backup.sql

# Restore database
docker-compose exec -T postgres psql -U postgres cicd_dashboard < backup.sql
```

### Bind Mounts

Application logs are mounted to host:
```yaml
volumes:
  - ./logs:/app/logs
```

Database initialization script:
```yaml
volumes:
  - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
```

## Health Checks

### PostgreSQL Health Check

```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U postgres"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Application Health Check

```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider",
         "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

**Check health status:**
```bash
docker-compose ps
docker inspect --format='{{json .State.Health}}' cicd-dashboard-app
```

**Access health endpoint:**
```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Container Won't Start

**Check logs:**
```bash
docker-compose logs app
docker-compose logs postgres
```

**Common issues:**

1. **Port already in use:**
   ```bash
   # Check what's using port 8080
   lsof -i :8080  # Linux/Mac
   netstat -ano | findstr :8080  # Windows

   # Change port in docker-compose.yml
   ports:
     - "8081:8080"
   ```

2. **Database connection failed:**
   ```bash
   # Check if postgres is healthy
   docker-compose ps

   # Check database logs
   docker-compose logs postgres

   # Verify connection from app
   docker-compose exec app sh
   nc -zv postgres 5432
   ```

3. **Out of memory:**
   ```bash
   # Check Docker resources
   docker stats

   # Increase memory in Docker Desktop settings
   # Or adjust JVM memory in docker-compose.yml
   JAVA_OPTS: "-Xms256m -Xmx512m"
   ```

### Build Failures

**Clear cache and rebuild:**
```bash
docker-compose build --no-cache
docker-compose up -d --force-recreate
```

**Check Maven build:**
```bash
docker-compose run --rm app mvn clean package
```

### Database Issues

**Reset database:**
```bash
docker-compose down -v
docker-compose up -d
```

**Access database:**
```bash
docker-compose exec postgres psql -U postgres -d cicd_dashboard

# List tables
\dt

# Describe table
\d builds

# Query data
SELECT * FROM repositories;
```

### Network Issues

**Inspect network:**
```bash
docker network ls
docker network inspect peraton-proj_cicd-network
```

**Test connectivity:**
```bash
docker-compose exec app ping postgres
docker-compose exec app nc -zv postgres 5432
```

### Permission Issues

**Linux:** If you encounter permission issues:
```bash
sudo chown -R $USER:$USER .
```

## Production Deployment

### Production Compose File

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    restart: always
    environment:
      POSTGRES_DB: cicd_dashboard
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - cicd-network
    # Don't expose port publicly in production

  app:
    build:
      context: .
      dockerfile: Dockerfile
    restart: always
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cicd_dashboard
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      GITHUB_TOKEN: ${GITHUB_TOKEN}
      SCHEDULER_ENABLED: "true"
      SPRING_PROFILES_ACTIVE: prod
      LOGGING_LEVEL_COM_PERATON_CICD: INFO
    ports:
      - "8080:8080"
    networks:
      - cicd-network
    volumes:
      - ./logs:/app/logs

networks:
  cicd-network:
    driver: bridge

volumes:
  postgres_data:
```

### Deploy to Production

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Security Best Practices

1. **Use secrets instead of environment variables:**
   ```yaml
   secrets:
     db_password:
       file: ./secrets/db_password.txt
   ```

2. **Don't expose database port:**
   Remove `ports:` from postgres service

3. **Use strong passwords:**
   ```bash
   # Generate secure password
   openssl rand -base64 32
   ```

4. **Enable SSL for database:**
   ```yaml
   SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cicd_dashboard?sslmode=require
   ```

5. **Run behind reverse proxy:**
   ```yaml
   nginx:
     image: nginx:alpine
     ports:
       - "80:80"
       - "443:443"
     volumes:
       - ./nginx.conf:/etc/nginx/nginx.conf
   ```

### Resource Limits

Add resource constraints:

```yaml
app:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 1G
      reservations:
        cpus: '0.5'
        memory: 512M
```

### Monitoring

Add monitoring services:

```yaml
prometheus:
  image: prom/prometheus
  ports:
    - "9090:9090"
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml

grafana:
  image: grafana/grafana
  ports:
    - "3000:3000"
  depends_on:
    - prometheus
```

## Docker Hub Deployment

### Build and Push Image

```bash
# Login to Docker Hub
docker login

# Tag image
docker tag cicd-dashboard:latest yourusername/cicd-dashboard:latest

# Push to Docker Hub
docker push yourusername/cicd-dashboard:latest
```

### Pull and Run

```bash
docker pull yourusername/cicd-dashboard:latest
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db \
  -e GITHUB_TOKEN=token \
  yourusername/cicd-dashboard:latest
```

## Kubernetes Deployment

See [KUBERNETES.md](KUBERNETES.md) for Kubernetes deployment guide.

## Related Documentation

- [Main README](README.md)
- [GitHub Integration](GITHUB_INTEGRATION.md)
- [Scheduling](SCHEDULING.md)
- [API Documentation](API_DOCUMENTATION.md)

## Support

For Docker-related issues:
1. Check logs: `docker-compose logs -f`
2. Verify health: `docker-compose ps`
3. Review this troubleshooting guide
4. Contact development team with logs and error messages
