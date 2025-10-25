# Scheduled Tasks Documentation

This document explains the scheduled task system for automatic GitHub Actions workflow synchronization.

## Overview

The CI/CD Pipeline Dashboard includes an automated scheduling system that periodically polls the GitHub API to fetch new workflow runs and saves them to the database. This ensures your dashboard always has the latest build information without manual intervention.

## Features

- **Automatic polling** - Fetches new builds from GitHub every 5 minutes (configurable)
- **Multiple repository support** - Monitor multiple repositories simultaneously
- **Error resilience** - Continues syncing other repositories if one fails
- **Enable/disable per repository** - Control which repositories to monitor
- **Manual trigger** - Trigger sync on-demand via REST API
- **Comprehensive logging** - Detailed logs for monitoring and debugging

## Configuration

### Basic Configuration

Edit `src/main/resources/application.yml`:

```yaml
scheduler:
  enabled: true                    # Enable/disable scheduler globally
  github-sync-rate: 300000         # Polling interval in milliseconds (5 minutes)
  repositories:
    - id: 1                        # Local database repository ID
      owner: microsoft              # GitHub repository owner
      repo: vscode                  # GitHub repository name
      enabled: true                 # Enable/disable this repository
    - id: 2
      owner: spring-projects
      repo: spring-boot
      enabled: true
```

### Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `scheduler.enabled` | boolean | true | Enable/disable scheduled tasks globally |
| `scheduler.github-sync-rate` | integer | 300000 | Sync interval in milliseconds (5 minutes) |
| `scheduler.repositories` | list | [] | List of repositories to monitor |
| `repositories[].id` | long | - | Database repository ID (required) |
| `repositories[].owner` | string | - | GitHub owner/organization (required) |
| `repositories[].repo` | string | - | GitHub repository name (required) |
| `repositories[].enabled` | boolean | true | Enable/disable this repository |

### Sync Intervals

Common intervals in milliseconds:

| Interval | Milliseconds | Configuration Value |
|----------|--------------|---------------------|
| 1 minute | 60,000 | `60000` |
| 5 minutes | 300,000 | `300000` |
| 10 minutes | 600,000 | `600000` |
| 15 minutes | 900,000 | `900000` |
| 30 minutes | 1,800,000 | `1800000` |
| 1 hour | 3,600,000 | `3600000` |

### Using Cron Expressions (Alternative)

You can also use cron expressions instead of fixed rates:

```yaml
scheduler:
  github-sync-cron: "0 */5 * * * *"  # Every 5 minutes
```

Cron format: `seconds minutes hours day month weekday`

Examples:
- `0 */5 * * * *` - Every 5 minutes
- `0 0 * * * *` - Every hour
- `0 0 */6 * * *` - Every 6 hours
- `0 0 9 * * MON-FRI` - 9 AM weekdays only

## Setup

### Step 1: Add Repositories to Database

First, add repositories you want to monitor:

```bash
curl -X POST http://localhost:8080/api/repositories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "vscode",
    "githubUrl": "https://github.com/microsoft/vscode"
  }'
```

Note the `id` from the response.

### Step 2: Configure Scheduler

Update `application.yml` with the repository details:

```yaml
scheduler:
  enabled: true
  github-sync-rate: 300000
  repositories:
    - id: 1                    # Use the ID from Step 1
      owner: microsoft
      repo: vscode
      enabled: true
```

### Step 3: Set GitHub Token

```bash
export GITHUB_TOKEN=ghp_your_token_here
```

### Step 4: Start Application

```bash
mvn spring-boot:run
```

The scheduler will start automatically and sync every 5 minutes.

## Monitoring

### Log Output

When the scheduler runs, you'll see output like:

```
2025-10-25 10:00:00.123 INFO  --- [scheduling-1] ScheduledTasksService : ========================================
2025-10-25 10:00:00.124 INFO  --- [scheduling-1] ScheduledTasksService : Starting scheduled GitHub builds sync at 2025-10-25T10:00:00
2025-10-25 10:00:00.125 INFO  --- [scheduling-1] ScheduledTasksService : ========================================
2025-10-25 10:00:00.126 INFO  --- [scheduling-1] ScheduledTasksService : Syncing repository: microsoft/vscode (ID: 1)
2025-10-25 10:00:02.456 INFO  --- [scheduling-1] ScheduledTasksService : Successfully synced 5 new builds for microsoft/vscode
2025-10-25 10:00:02.457 INFO  --- [scheduling-1] ScheduledTasksService : ========================================
2025-10-25 10:00:02.458 INFO  --- [scheduling-1] ScheduledTasksService : Scheduled GitHub sync completed at 2025-10-25T10:00:02
2025-10-25 10:00:02.459 INFO  --- [scheduling-1] ScheduledTasksService : Total builds synced: 5
2025-10-25 10:00:02.460 INFO  --- [scheduling-1] ScheduledTasksService : Successful repositories: 1
2025-10-25 10:00:02.461 INFO  --- [scheduling-1] ScheduledTasksService : Failed repositories: 0
2025-10-25 10:00:02.462 INFO  --- [scheduling-1] ScheduledTasksService : ========================================
```

### Application Logs

Check logs for scheduler status:

```bash
tail -f logs/application.log | grep ScheduledTasksService
```

### Database Verification

Check newly synced builds:

```bash
curl http://localhost:8080/api/builds
```

## Manual Triggering

You can manually trigger a sync without waiting for the scheduled time:

```bash
curl -X POST http://localhost:8080/api/scheduler/trigger-sync
```

**Response:**
```json
{
  "status": "success",
  "message": "GitHub sync triggered successfully. Check logs for details."
}
```

This is useful for:
- Testing the scheduler configuration
- Immediate sync after adding a new repository
- On-demand updates

## Disabling the Scheduler

### Temporarily Disable

Set in `application.yml`:

```yaml
scheduler:
  enabled: false
```

### Disable Specific Repository

```yaml
scheduler:
  repositories:
    - id: 1
      owner: microsoft
      repo: vscode
      enabled: false  # This repository won't be synced
```

### Disable via Environment Variable

```bash
export SCHEDULER_ENABLED=false
mvn spring-boot:run
```

## Error Handling

The scheduler is designed to be resilient:

### GitHub API Errors

If GitHub API is unavailable or rate limited:
- The error is logged
- Other repositories continue syncing
- The scheduler retries on the next interval

### Repository Not Found

If a repository is not found in the database:
- An error is logged
- Other repositories continue syncing
- The scheduler continues running

### Network Issues

If network connection fails:
- The error is logged
- The scheduler retries on the next interval

## Fallback Mode

If no repositories are configured in `application.yml`, the scheduler will attempt to sync **all repositories** from the database:

```
2025-10-25 10:00:00 WARN  --- [scheduling-1] ScheduledTasksService : No repositories configured for scheduled sync
2025-10-25 10:00:00 INFO  --- [scheduling-1] ScheduledTasksService : Attempting to sync all repositories from database
```

The scheduler will:
1. Fetch all repositories from the database
2. Extract owner/repo from the GitHub URL
3. Sync each repository

## Best Practices

### 1. Choose Appropriate Sync Interval

Consider:
- **GitHub API rate limits**: 5,000 requests/hour with token
- **Repository activity**: Active repos need more frequent syncing
- **Server load**: More frequent = higher load

**Recommendations:**
- Active repositories: 5-10 minutes
- Moderate activity: 15-30 minutes
- Low activity: 1 hour

### 2. Monitor Logs

Regularly check logs for:
- Sync success/failure rates
- GitHub API errors
- Rate limit warnings

### 3. GitHub Token Management

- Use a dedicated service account token
- Rotate tokens periodically
- Monitor token expiration

### 4. Database Maintenance

Periodically clean up old builds:
```sql
DELETE FROM builds WHERE started_at < NOW() - INTERVAL '90 days';
```

### 5. Resource Planning

Each sync operation:
- Makes 1 GitHub API request per repository
- Creates database transactions
- Consumes server resources

Plan accordingly for scale.

## Troubleshooting

### Scheduler Not Running

**Check if enabled:**
```bash
curl http://localhost:8080/actuator/scheduledtasks
```

**Verify configuration:**
```yaml
scheduler:
  enabled: true  # Must be true
```

**Check logs:**
```bash
grep "ScheduledTasksService" logs/application.log
```

### No Builds Being Synced

**Verify repositories configured:**
```yaml
scheduler:
  repositories:
    - id: 1  # Must match database ID
      owner: microsoft  # Must be correct
      repo: vscode  # Must be correct
```

**Check GitHub token:**
```bash
echo $GITHUB_TOKEN
```

**Test GitHub API manually:**
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://api.github.com/repos/microsoft/vscode/actions/runs
```

### Rate Limit Errors

**Error message:**
```
GitHub API error: 403 Forbidden - Rate limit exceeded
```

**Solutions:**
- Increase sync interval
- Reduce number of monitored repositories
- Wait for rate limit reset (hourly)

**Check rate limit status:**
```bash
curl -H "Authorization: Bearer $GITHUB_TOKEN" \
  https://api.github.com/rate_limit
```

### High Memory Usage

If memory usage is high:
1. Reduce sync frequency
2. Limit number of builds synced (configured in service)
3. Implement build retention policy
4. Increase JVM heap size

## Advanced Configuration

### Custom Thread Pool

```java
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
```

### Environment-Specific Configuration

**application-prod.yml:**
```yaml
scheduler:
  github-sync-rate: 600000  # 10 minutes in production
```

**application-dev.yml:**
```yaml
scheduler:
  github-sync-rate: 60000  # 1 minute in development
```

## Metrics and Monitoring

### Key Metrics to Track

- **Sync duration** - How long each sync takes
- **Success rate** - Percentage of successful syncs
- **Builds synced** - Number of new builds per sync
- **Error rate** - Frequency of errors

### Future Enhancements

Planned features:
- Prometheus metrics integration
- Slack/email notifications on failures
- Dashboard for scheduler statistics
- Adaptive sync intervals based on activity
- Webhook support instead of polling

## Example Configurations

### Single Repository (Simple)

```yaml
scheduler:
  enabled: true
  github-sync-rate: 300000
  repositories:
    - id: 1
      owner: myorg
      repo: myapp
      enabled: true
```

### Multiple Repositories

```yaml
scheduler:
  enabled: true
  github-sync-rate: 300000
  repositories:
    - id: 1
      owner: microsoft
      repo: vscode
      enabled: true
    - id: 2
      owner: spring-projects
      repo: spring-boot
      enabled: true
    - id: 3
      owner: facebook
      repo: react
      enabled: false  # Temporarily disabled
```

### High-Frequency Monitoring

```yaml
scheduler:
  enabled: true
  github-sync-rate: 60000  # 1 minute
  repositories:
    - id: 1
      owner: myorg
      repo: critical-app
      enabled: true
```

## Related Documentation

- [GitHub Integration Guide](GITHUB_INTEGRATION.md)
- [API Documentation](API_DOCUMENTATION.md)
- [Main README](README.md)

## Support

For issues or questions about the scheduler, check the logs first, then refer to the troubleshooting section. For additional support, contact the development team.
