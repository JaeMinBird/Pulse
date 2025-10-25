# Setup Guide - Getting Started

This guide walks you through setting up the CI/CD Pipeline Dashboard from scratch, including all required keys, tokens, and configurations.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [GitHub Personal Access Token](#github-personal-access-token)
3. [PostgreSQL Database Setup](#postgresql-database-setup)
4. [Environment Configuration](#environment-configuration)
5. [Backend Setup](#backend-setup)
6. [Frontend Setup](#frontend-setup)
7. [Docker Setup (Alternative)](#docker-setup-alternative)
8. [Verification](#verification)
9. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

**Backend:**
- **Java 17 or higher** - [Download from Oracle](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or use [OpenJDK](https://adoptium.net/)
- **Maven 3.6 or higher** - [Download Maven](https://maven.apache.org/download.cgi)
- **PostgreSQL 12 or higher** - [Download PostgreSQL](https://www.postgresql.org/download/)

**Frontend:**
- **Node.js 18+ and npm** - [Download Node.js](https://nodejs.org/)
- **Angular CLI** (optional, but recommended) - Install via npm: `npm install -g @angular/cli`

**Docker (Alternative to local setup):**
- **Docker 20.10+** - [Download Docker Desktop](https://www.docker.com/products/docker-desktop)
- **Docker Compose 2.0+** - Included with Docker Desktop

### Verify Installations

```bash
# Check Java
java -version
# Should show: java version "17.x.x"

# Check Maven
mvn -version
# Should show: Apache Maven 3.x.x

# Check PostgreSQL
psql --version
# Should show: psql (PostgreSQL) 12.x or higher

# Check Node.js and npm
node -v
# Should show: v18.x.x or higher

npm -v
# Should show: 9.x.x or higher

# Check Docker (if using Docker)
docker --version
docker-compose --version
```

---

## GitHub Personal Access Token

The application uses the GitHub REST API to fetch workflow run data. You need a Personal Access Token (PAT) to authenticate.

### Step 1: Create a GitHub Account

If you don't have one: [Sign up for GitHub](https://github.com/signup)

### Step 2: Generate a Personal Access Token

1. **Log in to GitHub**
2. **Go to Settings**
   - Click your profile picture (top-right) → **Settings**

3. **Navigate to Developer Settings**
   - Scroll down to **Developer settings** (left sidebar)

4. **Create a Personal Access Token**
   - Click **Personal access tokens** → **Tokens (classic)**
   - Click **Generate new token** → **Generate new token (classic)**

5. **Configure Token Settings**
   - **Note:** `CI/CD Dashboard API Access`
   - **Expiration:** Choose appropriate duration (e.g., 90 days, 1 year, or No expiration)

6. **Select Scopes (Permissions)**

   For **public repositories only**, select:
   - ✅ `public_repo` - Access public repositories

   For **private repositories**, select:
   - ✅ `repo` (all) - Full control of private repositories
     - This includes: `repo:status`, `repo_deployment`, `public_repo`, `repo:invite`, `security_events`

   For **organization repositories**, additionally select:
   - ✅ `read:org` - Read org and team membership, read org projects

7. **Generate and Copy Token**
   - Click **Generate token** at the bottom
   - **IMPORTANT:** Copy the token immediately (starts with `ghp_`)
   - You won't be able to see it again!

### Step 3: Store Token Securely

**Example token format:** `ghp_1234567890abcdefghijklmnopqrstuvwxyzAB`

**⚠️ Security Warning:**
- Never commit tokens to Git repositories
- Never share tokens publicly
- Rotate tokens periodically
- Use minimal required permissions

---

## PostgreSQL Database Setup

### Step 1: Start PostgreSQL Service

**Windows:**
```bash
# PostgreSQL should start automatically after installation
# Or start via Services app: PostgreSQL 15
```

**macOS:**
```bash
# If installed via Homebrew
brew services start postgresql@15
```

**Linux:**
```bash
sudo systemctl start postgresql
sudo systemctl enable postgresql  # Start on boot
```

### Step 2: Create Database User (Optional)

By default, PostgreSQL creates a `postgres` superuser. You can use this or create a dedicated user.

**Option A: Use default `postgres` user** (easiest)
- Skip to Step 3

**Option B: Create dedicated user** (recommended for production)

```bash
# Connect to PostgreSQL
psql -U postgres

# Inside psql console:
CREATE USER cicd_user WITH PASSWORD 'your_secure_password';
CREATE DATABASE cicd_dashboard OWNER cicd_user;
GRANT ALL PRIVILEGES ON DATABASE cicd_dashboard TO cicd_user;

# Exit psql
\q
```

### Step 3: Create Databases

**Using default `postgres` user:**

```bash
# Connect to PostgreSQL
psql -U postgres

# Create production database
CREATE DATABASE cicd_dashboard;

# Create development database (optional)
CREATE DATABASE cicd_dashboard_dev;

# Exit
\q
```

**Or using command line:**

```bash
# Windows
createdb -U postgres cicd_dashboard
createdb -U postgres cicd_dashboard_dev

# macOS/Linux
createdb cicd_dashboard
createdb cicd_dashboard_dev
```

### Step 4: Set PostgreSQL Password (if needed)

If you haven't set a password for the `postgres` user:

```bash
# Connect to PostgreSQL
psql -U postgres

# Set password
ALTER USER postgres WITH PASSWORD 'your_password';

# Exit
\q
```

### Step 5: Verify Database Connection

```bash
# Test connection
psql -U postgres -d cicd_dashboard -c "SELECT version();"

# Should show PostgreSQL version info
```

---

## Environment Configuration

You need to configure the application with your database credentials and GitHub token.

### Option 1: Environment Variables (Recommended)

**Windows (PowerShell):**
```powershell
# Set for current session
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_postgres_password"
$env:GITHUB_TOKEN = "ghp_your_github_token_here"

# Set permanently (System Environment Variables)
[System.Environment]::SetEnvironmentVariable('DB_USERNAME', 'postgres', 'User')
[System.Environment]::SetEnvironmentVariable('DB_PASSWORD', 'your_password', 'User')
[System.Environment]::SetEnvironmentVariable('GITHUB_TOKEN', 'ghp_your_token', 'User')
```

**Windows (Command Prompt):**
```cmd
set DB_USERNAME=postgres
set DB_PASSWORD=your_postgres_password
set GITHUB_TOKEN=ghp_your_github_token_here
```

**macOS/Linux (Bash):**
```bash
# Add to ~/.bashrc or ~/.bash_profile or ~/.zshrc
export DB_USERNAME=postgres
export DB_PASSWORD=your_postgres_password
export GITHUB_TOKEN=ghp_your_github_token_here

# Apply changes
source ~/.bashrc  # or ~/.zshrc
```

### Option 2: .env File (For Docker)

Create a `.env` file in the project root:

```bash
# Copy example file
cp .env.example .env
```

Edit `.env`:
```properties
# Database Configuration
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
DB_HOST=localhost
DB_PORT=5432
DB_NAME=cicd_dashboard

# GitHub API
GITHUB_TOKEN=ghp_your_github_token_here

# CORS (for frontend)
CORS_ALLOWED_ORIGINS=http://localhost:4200

# Scheduler
SCHEDULER_ENABLED=true
SCHEDULER_GITHUB_SYNC_RATE=300000
```

### Option 3: Application Configuration File (Not Recommended for Secrets)

**⚠️ Warning:** This exposes secrets in your codebase. Only use for local development.

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: postgres  # Replace with your username
    password: your_password  # Replace with your password

github:
  api:
    token: ghp_your_github_token_here  # Replace with your token
```

**IMPORTANT:** If you edit `application.yml` directly:
- Never commit this file with real credentials
- Add `application.yml` to `.gitignore` (or use `application-local.yml`)

---

## Backend Setup

### Step 1: Navigate to Project Directory

```bash
cd "C:\Users\rewhi\OneDrive\Desktop\Peraton Proj"
```

### Step 2: Verify Environment Variables

**Windows (PowerShell):**
```powershell
echo $env:DB_USERNAME
echo $env:DB_PASSWORD
echo $env:GITHUB_TOKEN
```

**macOS/Linux:**
```bash
echo $DB_USERNAME
echo $DB_PASSWORD
echo $GITHUB_TOKEN
```

### Step 3: Build the Project

```bash
# Clean and build
mvn clean install

# Or skip tests for faster build
mvn clean install -DskipTests
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

### Step 4: Run the Application

```bash
# Run with default profile
mvn spring-boot:run

# Or run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Expected output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.2.0)

... Tomcat started on port(s): 8080 (http) ...
... Started CicdDashboardApplication in 12.345 seconds ...
```

### Step 5: Verify Backend is Running

Open a browser or use curl:

```bash
# Test health endpoint
curl http://localhost:8080/api/builds

# Or test GitHub integration
curl http://localhost:8080/api/github/status/microsoft/vscode
```

**Expected:** JSON response with build data (may be empty initially)

---

## Frontend Setup

### Step 1: Navigate to Frontend Directory

```bash
cd frontend
```

### Step 2: Install Dependencies

```bash
npm install
```

**Expected output:**
```
added 1234 packages in 45s
```

### Step 3: Verify Environment Configuration

Check `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'  // Should point to backend
};
```

### Step 4: Run the Frontend

```bash
npm start

# Or explicitly
ng serve
```

**Expected output:**
```
** Angular Live Development Server is listening on localhost:4200 **
✔ Compiled successfully.
```

### Step 5: Access the Dashboard

Open browser and navigate to:
```
http://localhost:4200
```

You should see the CI/CD Dashboard with repository cards.

---

## Docker Setup (Alternative)

Skip backend and frontend setup above if using Docker.

### Step 1: Create .env File

```bash
cp .env.example .env
```

Edit `.env` with your credentials:
```properties
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
GITHUB_TOKEN=ghp_your_github_token_here
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

### Step 2: Start with Docker Compose

```bash
# Start all services (backend + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

### Step 3: Verify Services

```bash
# Check backend
curl http://localhost:8080/api/builds

# Check PostgreSQL
docker-compose exec db psql -U postgres -d cicd_dashboard -c "SELECT 1;"
```

### Step 4: Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (database data)
docker-compose down -v
```

---

## Verification

### Complete Setup Checklist

- [ ] Java 17+ installed
- [ ] Maven 3.6+ installed
- [ ] PostgreSQL 12+ installed and running
- [ ] Node.js 18+ and npm installed
- [ ] GitHub Personal Access Token generated
- [ ] PostgreSQL databases created (`cicd_dashboard`, `cicd_dashboard_dev`)
- [ ] Environment variables set (`DB_USERNAME`, `DB_PASSWORD`, `GITHUB_TOKEN`)
- [ ] Backend builds successfully (`mvn clean install`)
- [ ] Backend runs on http://localhost:8080
- [ ] Frontend dependencies installed (`npm install`)
- [ ] Frontend runs on http://localhost:4200
- [ ] No CORS errors in browser console
- [ ] Dashboard loads and displays repositories

### Test API Endpoints

```bash
# 1. Get all builds
curl http://localhost:8080/api/builds

# 2. Get GitHub workflow status for a public repository
curl http://localhost:8080/api/github/status/microsoft/vscode

# 3. Get all repositories
curl http://localhost:8080/api/repositories

# 4. Check scheduler status
curl http://localhost:8080/api/scheduler/status
```

### Test Frontend

1. Open browser: http://localhost:4200
2. Open DevTools (F12) → Console tab
3. Check for errors:
   - ✅ No CORS errors
   - ✅ No 404 errors for API calls
   - ✅ Data loads successfully

---

## Troubleshooting

### Backend Won't Start

**Error: `Connection to localhost:5432 refused`**

**Solution:**
- PostgreSQL is not running
- Start PostgreSQL service:
  ```bash
  # Windows: Start via Services app
  # macOS: brew services start postgresql@15
  # Linux: sudo systemctl start postgresql
  ```

**Error: `password authentication failed for user "postgres"`**

**Solution:**
- Wrong password in environment variables
- Verify password: `psql -U postgres` and enter password
- Update environment variables with correct password

**Error: `database "cicd_dashboard" does not exist`**

**Solution:**
- Database not created
- Create database:
  ```bash
  psql -U postgres -c "CREATE DATABASE cicd_dashboard;"
  ```

### GitHub API Errors

**Error: `401 Unauthorized` or `Bad credentials`**

**Solution:**
- Invalid or expired GitHub token
- Generate new token: [GitHub Settings](https://github.com/settings/tokens)
- Update `GITHUB_TOKEN` environment variable

**Error: `403 Forbidden` or `API rate limit exceeded`**

**Solution:**
- GitHub API rate limit reached (60 requests/hour without token)
- Ensure `GITHUB_TOKEN` is set correctly
- Wait for rate limit to reset (check response headers)

**Error: `404 Not Found` when fetching repository**

**Solution:**
- Repository doesn't exist or is private
- For private repos, ensure token has `repo` scope
- Verify repository name: `owner/repo` format

### Frontend Issues

**Error: `CORS policy: No 'Access-Control-Allow-Origin' header`**

**Solution:**
- Backend not running
- Start backend: `mvn spring-boot:run`
- Verify CORS configuration in `application.yml`:
  ```yaml
  cors:
    allowed-origins: http://localhost:4200
  ```

**Error: `Failed to load resource: net::ERR_CONNECTION_REFUSED`**

**Solution:**
- Backend not running on port 8080
- Check backend status: `curl http://localhost:8080/api/builds`
- Start backend if not running

**Error: Frontend shows no data**

**Solution:**
- No repositories or builds in database yet
- Add a repository via API:
  ```bash
  curl -X POST http://localhost:8080/api/repositories \
    -H "Content-Type: application/json" \
    -d '{
      "name": "vscode",
      "githubUrl": "https://github.com/microsoft/vscode"
    }'
  ```
- Trigger sync:
  ```bash
  curl -X POST "http://localhost:8080/api/github/sync/microsoft/vscode?repositoryId=1"
  ```

### Docker Issues

**Error: `port 5432 is already allocated`**

**Solution:**
- PostgreSQL already running locally
- Stop local PostgreSQL:
  ```bash
  # Windows: Stop via Services app
  # macOS: brew services stop postgresql@15
  # Linux: sudo systemctl stop postgresql
  ```
- Or change Docker PostgreSQL port in `docker-compose.yml`

**Error: `GITHUB_TOKEN not set`**

**Solution:**
- `.env` file not created or missing `GITHUB_TOKEN`
- Create `.env` file with token:
  ```bash
  echo "GITHUB_TOKEN=ghp_your_token_here" >> .env
  ```

### Port Conflicts

**Error: `Port 8080 is already in use`**

**Solution:**
- Another application using port 8080
- Find and stop the application:
  ```bash
  # Windows
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F

  # macOS/Linux
  lsof -ti:8080
  kill -9 <PID>
  ```
- Or change Spring Boot port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

**Error: `Port 4200 is already in use`**

**Solution:**
- Another Angular app running
- Stop other Angular apps or use different port:
  ```bash
  ng serve --port 4201
  ```

---

## Next Steps

After successful setup:

1. **Configure Repositories for Monitoring**
   - Edit `application.yml` to add repositories:
     ```yaml
     scheduler:
       enabled: true
       repositories:
         - id: 1
           owner: microsoft
           repo: vscode
           enabled: true
     ```

2. **Add Repositories via API**
   - See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

3. **Configure Scheduling**
   - See [SCHEDULING.md](SCHEDULING.md)

4. **Deploy to Production**
   - See [DOCKER.md](DOCKER.md) for Docker deployment
   - Update `environment.prod.ts` with production API URL

5. **Explore GitHub Integration**
   - See [GITHUB_INTEGRATION.md](GITHUB_INTEGRATION.md)

---

## Quick Reference

### Environment Variables Summary

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_USERNAME` | No | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | No | `postgres` | PostgreSQL password |
| `DB_HOST` | No | `localhost` | PostgreSQL host |
| `DB_PORT` | No | `5432` | PostgreSQL port |
| `GITHUB_TOKEN` | Yes* | - | GitHub Personal Access Token |
| `CORS_ALLOWED_ORIGINS` | No | `http://localhost:4200` | Allowed frontend origins |
| `SCHEDULER_ENABLED` | No | `true` | Enable/disable scheduler |

\* Required for GitHub integration features

### Default Ports

| Service | Port | URL |
|---------|------|-----|
| Spring Boot Backend | 8080 | http://localhost:8080 |
| Angular Frontend | 4200 | http://localhost:4200 |
| PostgreSQL | 5432 | localhost:5432 |

### Key Files

| File | Purpose |
|------|---------|
| `application.yml` | Spring Boot main configuration |
| `application-dev.yml` | Development profile settings |
| `.env` | Docker environment variables |
| `frontend/src/environments/environment.ts` | Angular dev config |
| `frontend/src/environments/environment.prod.ts` | Angular prod config |

---

## Support

For additional help:
- Check [README.md](README.md) for project overview
- See [GITHUB_INTEGRATION.md](GITHUB_INTEGRATION.md) for GitHub setup details
- See [CORS_SETUP.md](CORS_SETUP.md) for frontend/backend integration
- See [DOCKER.md](DOCKER.md) for Docker deployment

---

**You're all set! 🎉**

Run the application:
```bash
# Terminal 1 - Backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend && npm start

# Access at http://localhost:4200
```
