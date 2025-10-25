# GitHub Actions Integration Guide

This document explains how to integrate the CI/CD Pipeline Dashboard with GitHub Actions.

## Overview

The application fetches workflow run data from GitHub Actions using the GitHub REST API. It provides real-time build status monitoring and can sync workflow runs to your local database.

## Setup

### 1. Generate GitHub Personal Access Token

1. Go to GitHub Settings → Developer Settings → Personal Access Tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give your token a descriptive name (e.g., "CI/CD Dashboard")
4. Select the following scopes:
   - `repo` (Full control of private repositories)
   - `workflow` (Update GitHub Action workflows)
   - `read:org` (Read org and team membership, if using organization repos)
5. Click "Generate token"
6. **Copy the token immediately** (you won't be able to see it again)

### 2. Configure the Application

There are three ways to provide your GitHub token:

#### Option A: Environment Variable (Recommended for Production)

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

Or on Windows:
```cmd
set GITHUB_TOKEN=ghp_your_token_here
```

#### Option B: Application Configuration File

Edit `src/main/resources/application.yml`:
```yaml
github:
  api:
    token: ghp_your_token_here
```

⚠️ **WARNING**: Do not commit your token to version control!

#### Option C: Application Properties (Development)

Create `src/main/resources/application-local.yml`:
```yaml
github:
  api:
    token: ghp_your_token_here
```

Then run with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Add `application-local.yml` to `.gitignore` (already configured).

## API Endpoints

### Get Latest Build Status

Get the most recent workflow run for a repository.

**Endpoint:** `GET /api/github/status/{owner}/{repo}`

**Example:**
```bash
curl http://localhost:8080/api/github/status/microsoft/vscode
```

**Response:**
```json
{
  "runId": 1234567890,
  "repositoryName": "vscode",
  "branch": "main",
  "commitSha": "abc123def456...",
  "status": "completed",
  "conclusion": "success",
  "runNumber": 1234,
  "startedAt": "2025-10-25T10:30:00",
  "updatedAt": "2025-10-25T10:45:00",
  "htmlUrl": "https://github.com/microsoft/vscode/actions/runs/1234567890",
  "commitMessage": "Fix build issue",
  "authorName": "John Doe"
}
```

### Get Latest Build Status for Branch

Get the most recent workflow run for a specific branch.

**Endpoint:** `GET /api/github/status/{owner}/{repo}/branch/{branch}`

**Example:**
```bash
curl http://localhost:8080/api/github/status/microsoft/vscode/branch/main
```

### Get All Workflow Runs

Get multiple workflow runs for a repository.

**Endpoint:** `GET /api/github/runs/{owner}/{repo}?perPage=10`

**Parameters:**
- `perPage` (optional): Number of results (default: 10, max: 100)

**Example:**
```bash
curl http://localhost:8080/api/github/runs/microsoft/vscode?perPage=20
```

### Get Workflow Runs by Status

Filter workflow runs by status.

**Endpoint:** `GET /api/github/runs/{owner}/{repo}/status/{status}?perPage=10`

**Status values:**
- `queued` - Workflow is queued
- `in_progress` - Workflow is running
- `completed` - Workflow has completed

**Example:**
```bash
curl http://localhost:8080/api/github/runs/microsoft/vscode/status/completed?perPage=5
```

### Sync Workflow Runs to Database

Sync workflow runs from GitHub to your local database.

**Endpoint:** `POST /api/github/sync/{owner}/{repo}?repositoryId=1`

**Parameters:**
- `repositoryId`: Local database ID of the repository

**Example:**
```bash
curl -X POST http://localhost:8080/api/github/sync/microsoft/vscode?repositoryId=1
```

**Response:**
```json
{
  "success": true,
  "syncedCount": 15,
  "message": "Synced 15 builds to database"
}
```

## GitHub API Status Mapping

The application maps GitHub workflow statuses to internal build statuses:

| GitHub Status | GitHub Conclusion | Internal Status |
|---------------|-------------------|-----------------|
| queued | - | PENDING |
| in_progress | - | IN_PROGRESS |
| completed | success | SUCCESS |
| completed | failure | FAILED |
| completed | cancelled | CANCELLED |

## Rate Limits

GitHub API has rate limits:
- **Authenticated requests**: 5,000 requests per hour
- **Unauthenticated requests**: 60 requests per hour

The application uses authenticated requests with your personal access token, giving you 5,000 requests/hour.

Check your rate limit status:
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" https://api.github.com/rate_limit
```

## Error Handling

The application handles various GitHub API errors:

| HTTP Status | Description | Application Response |
|-------------|-------------|---------------------|
| 200 | Success | Returns data |
| 401 | Unauthorized | Invalid or missing token |
| 403 | Forbidden | Rate limit exceeded or insufficient permissions |
| 404 | Not Found | Repository or workflow run not found |
| 422 | Validation Failed | Invalid parameters |
| 500 | Server Error | GitHub API error |

## Example Workflow

### 1. Add a Repository to the Database

```bash
curl -X POST http://localhost:8080/api/repositories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-app",
    "githubUrl": "https://github.com/myorg/my-app"
  }'
```

Response:
```json
{
  "id": 1,
  "name": "my-app",
  "githubUrl": "https://github.com/myorg/my-app",
  "createdAt": "2025-10-25T10:00:00"
}
```

### 2. Check Latest Build Status

```bash
curl http://localhost:8080/api/github/status/myorg/my-app
```

### 3. Sync Workflow Runs

```bash
curl -X POST http://localhost:8080/api/github/sync/myorg/my-app?repositoryId=1
```

### 4. View Synced Builds in Database

```bash
curl http://localhost:8080/api/builds/repository/1
```

## Security Best Practices

1. **Never commit tokens to version control**
   - Use environment variables
   - Add token files to `.gitignore`

2. **Use fine-grained tokens** (if available)
   - Limit access to specific repositories
   - Set expiration dates

3. **Rotate tokens regularly**
   - Generate new tokens periodically
   - Revoke old tokens

4. **Monitor token usage**
   - Check GitHub audit log
   - Monitor application logs

5. **Use read-only tokens when possible**
   - Only request necessary scopes
   - For this application, read-only access is sufficient

## Troubleshooting

### Token Authentication Failed

**Error:** `401 Unauthorized`

**Solution:**
- Verify token is correctly set in environment or config
- Ensure token has not expired
- Check token has correct scopes (`repo`, `workflow`)

### Rate Limit Exceeded

**Error:** `403 Forbidden - Rate limit exceeded`

**Solution:**
- Wait for rate limit to reset (resets hourly)
- Reduce polling frequency
- Use conditional requests with ETags

### Repository Not Found

**Error:** `404 Not Found`

**Solution:**
- Verify repository owner and name are correct
- Ensure your token has access to the repository
- For private repos, ensure token has `repo` scope

### No Workflow Runs Found

**Error:** `No workflow runs found for repository`

**Solution:**
- Verify the repository has GitHub Actions workflows
- Check if workflows have been executed
- Ensure workflows are in `.github/workflows/` directory

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub REST API Documentation](https://docs.github.com/en/rest)
- [GitHub Personal Access Tokens](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)

## Configuration Reference

### application.yml

```yaml
github:
  api:
    base-url: https://api.github.com      # GitHub API base URL
    token: ${GITHUB_TOKEN:placeholder}     # GitHub personal access token
    timeout: 10000                         # Request timeout in milliseconds
```

### Environment Variables

- `GITHUB_TOKEN` - Your GitHub personal access token
- `DB_USERNAME` - Database username (default: postgres)
- `DB_PASSWORD` - Database password (default: postgres)
