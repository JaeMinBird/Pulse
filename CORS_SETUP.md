# CORS Configuration Guide

This guide explains the CORS (Cross-Origin Resource Sharing) configuration for connecting the Angular frontend to the Spring Boot backend.

## Overview

CORS is required because the Angular frontend (http://localhost:4200) and Spring Boot backend (http://localhost:8080) run on different ports, making them different origins from the browser's perspective.

## Configuration

### Backend (Spring Boot)

#### 1. CORS Configuration Class

**Location:** `src/main/java/com/peraton/cicd/config/CorsConfig.java`

This configuration allows the Angular frontend to make requests to the backend API.

**Features:**
- Configurable allowed origins via `application.yml`
- Allows credentials (cookies, auth headers)
- Supports all common HTTP methods (GET, POST, PUT, DELETE, etc.)
- Exposes necessary headers
- Caches preflight requests for 1 hour

**Default Configuration:**
```java
@Value("${cors.allowed-origins:http://localhost:4200}")
private String allowedOrigins;
```

#### 2. Application Configuration

**Location:** `src/main/resources/application.yml`

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

**Default:** `http://localhost:4200` (Angular dev server)

**Multiple Origins:**
```yaml
cors:
  allowed-origins: http://localhost:4200,http://localhost:4201,https://your-domain.com
```

**Environment Variable:**
```bash
export CORS_ALLOWED_ORIGINS=http://localhost:4200,https://production-domain.com
```

### Frontend (Angular)

#### 1. Development Environment

**Location:** `frontend/src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

#### 2. Production Environment

**Location:** `frontend/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api'  // Update for production
};
```

**For production deployment, update to:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.your-domain.com/api'
};
```

## Local Development Setup

### 1. Start Backend

```bash
# Terminal 1 - Backend on port 8080
cd "Peraton Proj"
mvn spring-boot:run
```

Backend will be available at: `http://localhost:8080`

### 2. Start Frontend

```bash
# Terminal 2 - Frontend on port 4200
cd frontend
npm install
npm start
```

Frontend will be available at: `http://localhost:4200`

### 3. Verify CORS

Open browser console (F12) and check for CORS errors:
- ✅ **No errors** - CORS is working
- ❌ **CORS errors** - Follow troubleshooting below

## Testing CORS Configuration

### Using Browser

1. Open Angular app: `http://localhost:4200`
2. Open browser DevTools (F12) → Network tab
3. Refresh page
4. Check API calls to `http://localhost:8080/api/*`
5. Verify response headers include:
   - `Access-Control-Allow-Origin: http://localhost:4200`
   - `Access-Control-Allow-Credentials: true`

### Using curl

Test OPTIONS request (preflight):
```bash
curl -H "Origin: http://localhost:4200" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     --verbose \
     http://localhost:8080/api/builds
```

Expected response headers:
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

Test actual GET request:
```bash
curl -H "Origin: http://localhost:4200" \
     --verbose \
     http://localhost:8080/api/builds
```

## Configuration Options

### Allowed Origins

#### Development (Default)
```yaml
cors:
  allowed-origins: http://localhost:4200
```

#### Development with Multiple Ports
```yaml
cors:
  allowed-origins: http://localhost:4200,http://localhost:4201
```

#### Production
```yaml
cors:
  allowed-origins: https://dashboard.your-domain.com
```

#### Mixed (Dev + Prod)
```yaml
cors:
  allowed-origins: http://localhost:4200,https://dashboard.your-domain.com
```

#### Environment-based
```bash
# Development
export CORS_ALLOWED_ORIGINS=http://localhost:4200

# Production
export CORS_ALLOWED_ORIGINS=https://dashboard.your-domain.com
```

### Allowed Methods

Default configuration allows:
- GET
- POST
- PUT
- DELETE
- OPTIONS
- PATCH

To customize, edit `CorsConfig.java`:
```java
config.setAllowedMethods(Arrays.asList("GET", "POST"));
```

### Allowed Headers

Default: All headers (`*`)

To customize:
```java
config.setAllowedHeaders(Arrays.asList(
    "Content-Type",
    "Authorization",
    "X-Requested-With"
));
```

## Troubleshooting

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Cause:** Backend CORS not configured or frontend origin not allowed

**Solution:**
1. Verify backend is running: `curl http://localhost:8080/api/builds`
2. Check `application.yml` has CORS configuration
3. Verify origin matches exactly (including protocol and port)
4. Restart Spring Boot application

### Error: "Preflight request doesn't pass"

**Cause:** OPTIONS request not handled correctly

**Solution:**
1. Ensure `CorsConfig.java` exists in config package
2. Check Spring Boot logs for CORS filter registration
3. Verify OPTIONS method is in allowed methods

### Frontend Shows "Failed to load"

**Cause:** Wrong API URL or backend not running

**Solution:**
1. Check `environment.ts` has correct API URL
2. Verify backend is running: `http://localhost:8080/api/builds`
3. Check browser console for actual error

### Different Port Numbers

If Angular runs on different port (e.g., 4201):

**Update application.yml:**
```yaml
cors:
  allowed-origins: http://localhost:4201
```

**Or use environment variable:**
```bash
export CORS_ALLOWED_ORIGINS=http://localhost:4201
mvn spring-boot:run
```

### CORS Works in Dev but Not Production

**Cause:** Production URL not in allowed origins

**Solution:**

1. Update production configuration:
```yaml
cors:
  allowed-origins: https://your-production-domain.com
```

2. Update Angular production environment:
```typescript
// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://api.your-domain.com/api'
};
```

3. Build Angular with production config:
```bash
ng build --configuration production
```

## Production Deployment

### Scenario 1: Same Domain

**Setup:**
- Frontend: `https://your-domain.com`
- Backend: `https://your-domain.com/api`

**Configuration:**
```yaml
cors:
  allowed-origins: https://your-domain.com
```

**Angular:**
```typescript
export const environment = {
  production: true,
  apiUrl: '/api'  // Relative URL
};
```

**No CORS needed** - Use reverse proxy (Nginx/Apache)

### Scenario 2: Different Subdomains

**Setup:**
- Frontend: `https://dashboard.your-domain.com`
- Backend: `https://api.your-domain.com`

**Configuration:**
```yaml
cors:
  allowed-origins: https://dashboard.your-domain.com
```

**Angular:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.your-domain.com/api'
};
```

### Scenario 3: Different Domains

**Setup:**
- Frontend: `https://frontend.com`
- Backend: `https://backend.com`

**Configuration:**
```yaml
cors:
  allowed-origins: https://frontend.com
```

**Angular:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://backend.com/api'
};
```

## Docker Deployment

### Docker Compose with CORS

**docker-compose.yml:**
```yaml
services:
  backend:
    environment:
      CORS_ALLOWED_ORIGINS: http://localhost:4200,http://frontend:80

  frontend:
    environment:
      API_URL: http://backend:8080/api
```

### Kubernetes with CORS

**backend-configmap.yaml:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
data:
  CORS_ALLOWED_ORIGINS: "https://dashboard.your-domain.com"
```

## Security Best Practices

### 1. Specific Origins Only

❌ **Don't:**
```java
config.setAllowedOrigins(Arrays.asList("*"));  // Allows all origins
```

✅ **Do:**
```java
config.setAllowedOrigins(Arrays.asList("https://your-domain.com"));
```

### 2. Use HTTPS in Production

```yaml
cors:
  allowed-origins: https://dashboard.your-domain.com  # Not http://
```

### 3. Limit Methods

Only allow methods your API actually uses:
```java
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
```

### 4. Environment-Specific Configuration

Use different origins for dev/staging/prod:

**application-dev.yml:**
```yaml
cors:
  allowed-origins: http://localhost:4200
```

**application-prod.yml:**
```yaml
cors:
  allowed-origins: https://dashboard.your-domain.com
```

## Verification Checklist

- [ ] Backend running on `http://localhost:8080`
- [ ] Frontend running on `http://localhost:4200`
- [ ] `CorsConfig.java` exists and compiled
- [ ] `application.yml` has `cors.allowed-origins` configuration
- [ ] Angular `environment.ts` points to `http://localhost:8080/api`
- [ ] No CORS errors in browser console
- [ ] API calls returning data successfully
- [ ] Browser DevTools shows CORS headers in response

## Additional Resources

- [MDN CORS Documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring CORS Support](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-cors)
- [Angular HttpClient Guide](https://angular.io/guide/http)

## Support

If you continue to experience CORS issues:

1. Check backend logs for errors
2. Verify configuration in `application.yml`
3. Check browser console for detailed error messages
4. Use browser DevTools Network tab to inspect request/response headers
5. Test with curl to isolate frontend vs backend issues

## Summary

**Backend (Spring Boot):**
- `CorsConfig.java` - CORS filter configuration
- `application.yml` - `cors.allowed-origins: http://localhost:4200`

**Frontend (Angular):**
- `environment.ts` - `apiUrl: 'http://localhost:8080/api'`
- `environment.prod.ts` - Production API URL

**Testing:**
```bash
# Terminal 1
mvn spring-boot:run

# Terminal 2
cd frontend && npm start

# Terminal 3
curl -H "Origin: http://localhost:4200" http://localhost:8080/api/builds
```
