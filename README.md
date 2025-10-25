# CI/CD Pipeline Dashboard

A comprehensive Spring Boot application for monitoring and managing CI/CD pipelines. This dashboard provides real-time visibility into pipeline status, execution history, and deployment tracking.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **Additional Libraries**: Lombok, Spring Boot DevTools

## Features

- RESTful API for pipeline management
- **GitHub Actions Integration** - Real-time workflow monitoring
- **Automated Scheduling** - Polls GitHub API every 5 minutes for new builds
- PostgreSQL database integration
- CRUD operations for CI/CD pipelines and repositories
- Pipeline status tracking (PENDING, IN_PROGRESS, SUCCESS, FAILED, CANCELLED)
- Repository and branch monitoring
- Build history tracking with commit information
- Automatic timestamp management
- Workflow run synchronization to database
- Manual and automatic sync capabilities

## Project Structure

```
cicd-dashboard/
├── src/
│   ├── main/
│   │   ├── java/com/peraton/cicd/
│   │   │   ├── CicdDashboardApplication.java    # Main application entry point
│   │   │   ├── controller/                       # REST controllers
│   │   │   │   └── PipelineController.java
│   │   │   ├── service/                          # Business logic layer
│   │   │   │   └── PipelineService.java
│   │   │   ├── repository/                       # Data access layer
│   │   │   │   └── PipelineRepository.java
│   │   │   └── model/                            # Entity classes
│   │   │       └── Pipeline.java
│   │   └── resources/
│   │       ├── application.yml                   # Main configuration
│   │       └── application-dev.yml               # Development profile
│   └── test/
│       └── java/com/peraton/cicd/
│           └── CicdDashboardApplicationTests.java
├── pom.xml                                       # Maven dependencies
├── .gitignore
└── README.md
```

## Prerequisites

### Backend
- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- GitHub Personal Access Token (for GitHub Actions integration) - See [GITHUB_INTEGRATION.md](GITHUB_INTEGRATION.md)

### Frontend (Optional)
- Node.js 18+ and npm
- Angular CLI (optional, for development)

### Docker (Alternative)
- Docker 20.10 or higher
- Docker Compose 2.0 or higher
- GitHub Personal Access Token

**Documentation:**
- [SETUP_GUIDE.md](SETUP_GUIDE.md) - Complete setup guide with keys and tokens
- [DOCKER.md](DOCKER.md) - Docker deployment
- [CORS_SETUP.md](CORS_SETUP.md) - Frontend/Backend integration
- [frontend/README.md](frontend/README.md) - Angular frontend setup

## Database Setup

1. Install PostgreSQL if not already installed
2. Create a database for the application:

```sql
CREATE DATABASE cicd_dashboard;
```

For development environment:
```sql
CREATE DATABASE cicd_dashboard_dev;
```

3. Update database credentials in `src/main/resources/application.yml` or set environment variables:
   - `DB_USERNAME` (default: postgres)
   - `DB_PASSWORD` (default: postgres)

## Getting Started

### Quick Start with Docker (Recommended)

```bash
# 1. Clone the repository
git clone <repository-url>
cd "Peraton Proj"

# 2. Set up environment variables
cp .env.example .env
# Edit .env and add your GITHUB_TOKEN

# 3. Start the application
docker-compose up -d

# 4. Access the application
# http://localhost:8080/api/builds
```

See [DOCKER.md](DOCKER.md) for detailed Docker deployment guide.

### Local Development Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd "Peraton Proj"
```

### 2. Configure the database

Update the database connection settings in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cicd_dashboard
    username: your_username
    password: your_password
```

Or set environment variables:
```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### 3. Configure GitHub Integration (Optional)

To enable GitHub Actions integration, set your personal access token:

```bash
export GITHUB_TOKEN=ghp_your_github_personal_access_token_here
```

See [GITHUB_INTEGRATION.md](GITHUB_INTEGRATION.md) for detailed setup instructions.

### 4. Configure Scheduled Sync (Optional)

To enable automatic polling of GitHub for new builds, configure repositories in `application.yml`:

```yaml
scheduler:
  enabled: true
  github-sync-rate: 300000  # 5 minutes
  repositories:
    - id: 1                  # Database repository ID
      owner: microsoft
      repo: vscode
      enabled: true
```

See [SCHEDULING.md](SCHEDULING.md) for detailed scheduling documentation.

### 5. Build the project

```bash
mvn clean install
```

### 6. Run the application

```bash
mvn spring-boot:run
```

For development profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

The scheduled task will automatically start syncing configured repositories every 5 minutes.

## API Endpoints

### Build Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/builds` | Get all builds |
| GET | `/api/builds/{id}` | Get build by ID |
| GET | `/api/builds/repository/{repositoryId}` | Get builds by repository |
| GET | `/api/builds/status/{status}` | Get builds by status |
| POST | `/api/builds/sync` | Manually trigger GitHub sync |
| POST | `/api/scheduler/trigger-sync` | Manually trigger scheduled sync |

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

### GitHub Actions Integration

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/github/status/{owner}/{repo}` | Get latest build status |
| GET | `/api/github/status/{owner}/{repo}/branch/{branch}` | Get latest build status for branch |
| GET | `/api/github/runs/{owner}/{repo}` | Get all workflow runs |
| GET | `/api/github/runs/{owner}/{repo}/status/{status}` | Get workflow runs by status |
| POST | `/api/github/sync/{owner}/{repo}?repositoryId=1` | Sync workflow runs to database |

For detailed GitHub integration documentation, see [GITHUB_INTEGRATION.md](GITHUB_INTEGRATION.md).

### Pipeline Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pipelines` | Get all pipelines |
| GET | `/api/pipelines/{id}` | Get pipeline by ID |
| GET | `/api/pipelines/name/{name}` | Get pipeline by name |
| GET | `/api/pipelines/status/{status}` | Get pipelines by status |
| POST | `/api/pipelines` | Create a new pipeline |
| PUT | `/api/pipelines/{id}` | Update a pipeline |
| DELETE | `/api/pipelines/{id}` | Delete a pipeline |

### Example Request

Create a new pipeline:
```bash
curl -X POST http://localhost:8080/api/pipelines \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-app-pipeline",
    "description": "Main application build pipeline",
    "repository": "https://github.com/example/my-app",
    "branch": "main",
    "status": "PENDING"
  }'
```

### Pipeline Status Values

- `PENDING` - Pipeline is queued
- `IN_PROGRESS` - Pipeline is currently running
- `SUCCESS` - Pipeline completed successfully
- `FAILED` - Pipeline failed
- `CANCELLED` - Pipeline was cancelled

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -DskipTests
```

The JAR file will be created in the `target/` directory.

### Running the JAR

```bash
java -jar target/cicd-dashboard-0.0.1-SNAPSHOT.jar
```

## Configuration Profiles

The application supports multiple profiles:

- **default**: Production configuration
- **dev**: Development configuration with create-drop schema and verbose logging

Activate a profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Or when running the JAR:
```bash
java -jar target/cicd-dashboard-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## Logging

Logging levels can be configured in `application.yml`. Default levels:
- Application (com.peraton.cicd): DEBUG
- Spring Web: INFO
- Hibernate: INFO

## Deployment

### Docker Deployment

The application can be easily deployed using Docker:

```bash
docker-compose up -d
```

See [DOCKER.md](DOCKER.md) for:
- Complete Docker setup guide
- Production deployment configuration
- Troubleshooting and monitoring
- Security best practices

### Traditional Deployment

Deploy as a standalone JAR:

```bash
mvn clean package -DskipTests
java -jar target/cicd-dashboard-0.0.1-SNAPSHOT.jar
```

## Frontend Dashboard

An Angular 17+ frontend is available in the `frontend/` directory.

### Quick Start

```bash
# Terminal 1 - Start backend
mvn spring-boot:run

# Terminal 2 - Start frontend
cd frontend
npm install
npm start
```

Access the dashboard at `http://localhost:4200`

**Features:**
- Real-time repository and build monitoring
- Color-coded status indicators (green/red/yellow/blue)
- Auto-refresh every 30 seconds
- Manual sync from GitHub
- Responsive grid layout

**Documentation:** See [frontend/README.md](frontend/README.md) and [CORS_SETUP.md](CORS_SETUP.md)

## Future Enhancements

- Integration with additional CI/CD platforms (Jenkins, GitLab CI, CircleCI)
- Real-time WebSocket notifications for build status changes
- Pipeline execution history and analytics dashboard
- User authentication and authorization with JWT
- ~~Docker containerization~~ ✅ Completed
- ~~Frontend dashboard UI~~ ✅ Completed (Angular)
- Kubernetes deployment support
- Automated build triggering
- Slack/Teams notifications
- Build artifacts management

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please contact the development team.
