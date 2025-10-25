package com.peraton.cicd.service;

import com.peraton.cicd.config.GitHubApiConfig;
import com.peraton.cicd.dto.github.WorkflowRunsResponse;
import com.peraton.cicd.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiClient {

    @Qualifier("githubRestTemplate")
    private final RestTemplate restTemplate;
    private final GitHubApiConfig gitHubApiConfig;

    /**
     * Fetch workflow runs for a given repository
     *
     * @param owner Repository owner (username or organization)
     * @param repo Repository name
     * @return WorkflowRunsResponse containing list of workflow runs
     * @throws GitHubApiException if API call fails
     */
    public WorkflowRunsResponse getWorkflowRuns(String owner, String repo) {
        return getWorkflowRuns(owner, repo, null, null, null);
    }

    /**
     * Fetch workflow runs for a given repository with filters
     *
     * @param owner Repository owner
     * @param repo Repository name
     * @param branch Filter by branch name
     * @param status Filter by status (queued, in_progress, completed)
     * @param perPage Number of results per page (max 100)
     * @return WorkflowRunsResponse containing list of workflow runs
     * @throws GitHubApiException if API call fails
     */
    public WorkflowRunsResponse getWorkflowRuns(String owner, String repo, String branch, String status, Integer perPage) {
        String url = buildWorkflowRunsUrl(owner, repo, branch, status, perPage);

        log.debug("Fetching workflow runs from GitHub API: {}", url);

        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<WorkflowRunsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    WorkflowRunsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully fetched {} workflow runs", response.getBody().getTotalCount());
                return response.getBody();
            } else {
                throw new GitHubApiException("Unexpected response from GitHub API", response.getStatusCode().value());
            }

        } catch (HttpClientErrorException e) {
            log.error("GitHub API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GitHubApiException(
                    "Failed to fetch workflow runs: " + e.getMessage(),
                    e.getStatusCode().value(),
                    e
            );
        } catch (RestClientException e) {
            log.error("Error calling GitHub API", e);
            throw new GitHubApiException("Failed to connect to GitHub API: " + e.getMessage(), e);
        }
    }

    /**
     * Get a specific workflow run by ID
     *
     * @param owner Repository owner
     * @param repo Repository name
     * @param runId Workflow run ID
     * @return WorkflowRun details
     * @throws GitHubApiException if API call fails
     */
    public com.peraton.cicd.dto.github.WorkflowRun getWorkflowRun(String owner, String repo, Long runId) {
        String url = String.format("%s/repos/%s/%s/actions/runs/%d",
                gitHubApiConfig.getBaseUrl(), owner, repo, runId);

        log.debug("Fetching workflow run {} from GitHub API", runId);

        try {
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<com.peraton.cicd.dto.github.WorkflowRun> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    com.peraton.cicd.dto.github.WorkflowRun.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.debug("Successfully fetched workflow run {}", runId);
                return response.getBody();
            } else {
                throw new GitHubApiException("Unexpected response from GitHub API", response.getStatusCode().value());
            }

        } catch (HttpClientErrorException e) {
            log.error("GitHub API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GitHubApiException(
                    "Failed to fetch workflow run: " + e.getMessage(),
                    e.getStatusCode().value(),
                    e
            );
        } catch (RestClientException e) {
            log.error("Error calling GitHub API", e);
            throw new GitHubApiException("Failed to connect to GitHub API: " + e.getMessage(), e);
        }
    }

    private String buildWorkflowRunsUrl(String owner, String repo, String branch, String status, Integer perPage) {
        StringBuilder url = new StringBuilder(String.format(
                "%s/repos/%s/%s/actions/runs",
                gitHubApiConfig.getBaseUrl(), owner, repo
        ));

        boolean hasParams = false;

        if (branch != null && !branch.isEmpty()) {
            url.append("?branch=").append(branch);
            hasParams = true;
        }

        if (status != null && !status.isEmpty()) {
            url.append(hasParams ? "&" : "?").append("status=").append(status);
            hasParams = true;
        }

        if (perPage != null && perPage > 0) {
            url.append(hasParams ? "&" : "?").append("per_page=").append(Math.min(perPage, 100));
        }

        return url.toString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + gitHubApiConfig.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
