# CI/CD Pipeline Dashboard - Angular Frontend

A modern Angular 17+ frontend application for monitoring CI/CD pipelines and build statuses. Built with standalone components and Tailwind CSS.

## Features

- **Real-time Dashboard** - Monitor all repositories and their build statuses
- **Color-coded Status** - Visual indicators for build states:
  - 🟢 Green - Success
  - 🔴 Red - Failure
  - 🟡 Yellow - Pending
  - 🔵 Blue - In Progress (animated)
  - ⚫ Grey - Cancelled
- **Auto-refresh** - Automatic updates every 30 seconds
- **Manual Sync** - Trigger GitHub sync on-demand
- **Responsive Grid** - Adapts to different screen sizes
- **Standalone Components** - Modern Angular architecture
- **Tailwind CSS** - Utility-first styling

## Technology Stack

- **Angular**: 17.3+
- **TypeScript**: 5.4+
- **Tailwind CSS**: 3.4+
- **RxJS**: 7.8+

## Prerequisites

- Node.js 18+ and npm
- Running Spring Boot backend on `http://localhost:8080`

## Quick Start

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure API URL

Edit `src/environments/environment.ts` if your backend runs on a different port:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'  // Change if needed
};
```

### 3. Start Development Server

```bash
npm start
```

The application will open at `http://localhost:4200`

### 4. Build for Production

```bash
npm run build
```

Production files will be in `dist/cicd-dashboard-frontend/`

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── dashboard/              # Main dashboard component
│   │   │   │   ├── dashboard.component.ts
│   │   │   │   ├── dashboard.component.html
│   │   │   │   └── dashboard.component.css
│   │   │   └── repository-card/        # Repository card component
│   │   │       ├── repository-card.component.ts
│   │   │       ├── repository-card.component.html
│   │   │       └── repository-card.component.css
│   │   ├── models/
│   │   │   └── repository.model.ts     # TypeScript interfaces
│   │   ├── services/
│   │   │   └── api.service.ts          # API service
│   │   └── app.component.ts            # Root component
│   ├── environments/
│   │   └── environment.ts              # Environment config
│   ├── styles.css                      # Global styles + Tailwind
│   ├── index.html
│   └── main.ts                         # Application bootstrap
├── angular.json                        # Angular configuration
├── tailwind.config.js                  # Tailwind configuration
├── tsconfig.json                       # TypeScript configuration
├── package.json
└── README.md
```

## Components

### Dashboard Component

**Location:** `src/app/components/dashboard/`

Main component that displays all repositories in a responsive grid.

**Features:**
- Auto-refresh every 30 seconds
- Manual refresh button
- Sync from GitHub button
- Status summary (total, success, failed, in-progress, pending)
- Last updated timestamp
- Loading and error states

**Usage:**
```html
<app-dashboard></app-dashboard>
```

### Repository Card Component

**Location:** `src/app/components/repository-card/`

Individual card displaying repository and build information.

**Features:**
- Color-coded status indicator bar
- Animated spinner for in-progress builds
- Status badge with pulse animation
- Commit SHA (shortened)
- Build timestamps with relative time
- "View on GitHub" button
- Responsive hover effects

**Usage:**
```html
<app-repository-card [data]="repositoryWithBuild"></app-repository-card>
```

**Input:**
```typescript
@Input() data: RepositoryWithLatestBuild;
```

## Services

### API Service

**Location:** `src/app/services/api.service.ts`

Handles all HTTP communication with the Spring Boot backend.

**Methods:**

```typescript
// Repository endpoints
getAllRepositories(): Observable<Repository[]>
getRepositoryById(id: number): Observable<Repository>
createRepository(repository: Partial<Repository>): Observable<Repository>

// Build endpoints
getAllBuilds(): Observable<Build[]>
getBuildById(id: number): Observable<Build>
getBuildsByRepository(repositoryId: number): Observable<Build[]>
getBuildsByStatus(status: string): Observable<Build[]>
syncBuilds(owner: string, repo: string, repositoryId: number): Observable<SyncResponse>
triggerScheduledSync(): Observable<any>

// Combined endpoint
getRepositoriesWithLatestBuilds(): Observable<RepositoryWithLatestBuild[]>

// GitHub Actions endpoints
getGitHubStatus(owner: string, repo: string): Observable<any>
getGitHubWorkflowRuns(owner: string, repo: string, perPage?: number): Observable<any>
```

## Models

### TypeScript Interfaces

**Location:** `src/app/models/repository.model.ts`

```typescript
interface Repository {
  id: number;
  name: string;
  githubUrl: string;
  createdAt: string;
}

interface Build {
  id: number;
  repositoryId: number;
  repositoryName: string;
  status: BuildStatus;
  commitSha: string;
  startedAt: string;
  completedAt?: string;
}

type BuildStatus = 'PENDING' | 'IN_PROGRESS' | 'SUCCESS' | 'FAILED' | 'CANCELLED';

interface RepositoryWithLatestBuild {
  repository: Repository;
  latestBuild?: Build;
}
```

## Styling

### Tailwind CSS Configuration

**Location:** `tailwind.config.js`

Custom color palette for build statuses:

```javascript
colors: {
  'build-success': '#10b981',   // Green
  'build-failure': '#ef4444',   // Red
  'build-pending': '#eab308',   // Yellow
  'build-progress': '#3b82f6',  // Blue
  'build-cancelled': '#6b7280', // Gray
}
```

### Custom CSS Classes

**Location:** `src/styles.css`

```css
.status-badge  - Status badge styling
.card          - Card container styling
.btn-primary   - Primary button styling
.btn-secondary - Secondary button styling
```

## Development

### Run Development Server

```bash
npm start
# or
ng serve
```

Navigate to `http://localhost:4200`. The app will automatically reload on file changes.

### Run Tests

```bash
npm test
# or
ng test
```

### Lint Code

```bash
npm run lint
# or
ng lint
```

### Build

```bash
# Development build
ng build

# Production build
ng build --configuration production
```

## Configuration

### Environment Variables

**Development:** `src/environments/environment.ts`
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

**Production:** `src/environments/environment.prod.ts`
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://your-api-domain.com/api'
};
```

### Auto-refresh Interval

Edit `dashboard.component.ts`:

```typescript
refreshInterval = 30000; // milliseconds (default: 30 seconds)
```

## Features in Detail

### Auto-refresh

The dashboard automatically fetches new data every 30 seconds using RxJS interval:

```typescript
interval(this.refreshInterval)
  .pipe(
    startWith(0),
    switchMap(() => this.apiService.getRepositoriesWithLatestBuilds())
  )
  .subscribe(...)
```

Toggle on/off using the "Auto-refresh" button.

### Manual Sync

Trigger a GitHub sync manually:

```typescript
triggerSync(): void {
  this.apiService.triggerScheduledSync().subscribe(...)
}
```

This calls the backend's `/api/scheduler/trigger-sync` endpoint.

### Status Indicators

Each repository card shows:
- **Top bar** - Color-coded status indicator
- **Status icon** - Visual symbol in a circle
- **Status badge** - Text label with pulse animation for in-progress
- **Details** - Commit SHA, timestamps, duration

### Responsive Design

- **Mobile (< 768px)**: 1 column
- **Tablet (768px - 1024px)**: 2 columns
- **Desktop (> 1024px)**: 3 columns

## Troubleshooting

### CORS Errors

If you see CORS errors, add this to your Spring Boot application:

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

### Backend Not Running

Error message: "Failed to load repository data. Please check if the backend is running."

**Solution:**
1. Start the Spring Boot backend: `mvn spring-boot:run`
2. Verify it's running: `curl http://localhost:8080/api/builds`
3. Check the `apiUrl` in `environment.ts`

### No Repositories Showing

The dashboard shows "No repositories" when the backend has no data.

**Solution:**
1. Add repositories via the API:
   ```bash
   curl -X POST http://localhost:8080/api/repositories \
     -H "Content-Type: application/json" \
     -d '{"name":"test-repo","githubUrl":"https://github.com/user/repo"}'
   ```

2. Trigger a sync to fetch builds

### Build Fails

**Common issues:**

1. **Node modules not installed**
   ```bash
   npm install
   ```

2. **TypeScript errors**
   ```bash
   npm run lint
   ```

3. **Tailwind not working**
   - Verify `tailwind.config.js` exists
   - Check `styles.css` has Tailwind directives
   - Restart dev server

## Deployment

### Nginx Configuration

Example nginx config for serving the Angular app:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/cicd-dashboard/dist/cicd-dashboard-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

### Docker Deployment

Create `Dockerfile` in frontend directory:

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist/cicd-dashboard-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:
```bash
docker build -t cicd-dashboard-frontend .
docker run -p 80:80 cicd-dashboard-frontend
```

## API Endpoints Used

The frontend consumes these backend endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/repositories` | GET | Get all repositories |
| `/api/builds` | GET | Get all builds |
| `/api/builds/repository/{id}` | GET | Get builds for repository |
| `/api/builds/sync` | POST | Sync builds from GitHub |
| `/api/scheduler/trigger-sync` | POST | Trigger scheduled sync |

## Browser Support

- Chrome/Edge (latest)
- Firefox (latest)
- Safari (latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

## Performance

- **First Load**: ~200KB (gzipped)
- **Lazy Loading**: Components loaded on-demand
- **Change Detection**: OnPush strategy for better performance
- **HTTP Caching**: Leverages browser cache

## Contributing

1. Create a feature branch
2. Make changes
3. Run tests: `npm test`
4. Run linter: `npm run lint`
5. Build: `npm run build`
6. Submit pull request

## License

This project is licensed under the MIT License.

## Support

For issues or questions:
1. Check this README
2. Review the main project README
3. Check browser console for errors
4. Verify backend is running
5. Contact development team
