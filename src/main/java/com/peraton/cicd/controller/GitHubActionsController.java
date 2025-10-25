package com.peraton.cicd.controller;

import com.peraton.cicd.dto.github.BuildStatusDto;
import com.peraton.cicd.exception.GitHubApiException;
import com.peraton.cicd.service.GitHubActionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubActionsController {

    private final GitHubActionsService gitHubActionsService;

    /**
     * Get the latest build status for a repository
     * GET /api/github/status/{owner}/{repo}
     */
    @GetMapping("/status/{owner}/{repo}")
    public ResponseEntity<BuildStatusDto> getLatestBuildStatus(
            @PathVariable String owner,
            @PathVariable String repo) {
        try {
            BuildStatusDto status = gitHubActionsService.getLatestBuildStatus(owner, repo);
            return ResponseEntity.ok(status);
        } catch (GitHubApiException e) {
            return ResponseEntity.status(e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    /**
     * Get the latest build status for a specific branch
     * GET /api/github/status/{owner}/{repo}/branch/{branch}
     */
    @GetMapping("/status/{owner}/{repo}/branch/{branch}")
    public ResponseEntity<BuildStatusDto> getLatestBuildStatusForBranch(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String branch) {
        try {
            BuildStatusDto status = gitHubActionsService.getLatestBuildStatusForBranch(owner, repo, branch);
            return ResponseEntity.ok(status);
        } catch (GitHubApiException e) {
            return ResponseEntity.status(e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    /**
     * Get all workflow runs for a repository
     * GET /api/github/runs/{owner}/{repo}?perPage=10
     */
    @GetMapping("/runs/{owner}/{repo}")
    public ResponseEntity<List<BuildStatusDto>> getAllWorkflowRuns(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "10") Integer perPage) {
        try {
            List<BuildStatusDto> runs = gitHubActionsService.getAllWorkflowRuns(owner, repo, perPage);
            return ResponseEntity.ok(runs);
        } catch (GitHubApiException e) {
            return ResponseEntity.status(e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    /**
     * Get workflow runs filtered by status
     * GET /api/github/runs/{owner}/{repo}/status/{status}?perPage=10
     */
    @GetMapping("/runs/{owner}/{repo}/status/{status}")
    public ResponseEntity<List<BuildStatusDto>> getWorkflowRunsByStatus(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String status,
            @RequestParam(defaultValue = "10") Integer perPage) {
        try {
            List<BuildStatusDto> runs = gitHubActionsService.getWorkflowRunsByStatus(owner, repo, status, perPage);
            return ResponseEntity.ok(runs);
        } catch (GitHubApiException e) {
            return ResponseEntity.status(e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    /**
     * Sync workflow runs to database
     * POST /api/github/sync/{owner}/{repo}?repositoryId=1
     */
    @PostMapping("/sync/{owner}/{repo}")
    public ResponseEntity<Map<String, Object>> syncWorkflowRuns(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam Long repositoryId) {
        try {
            int syncedCount = gitHubActionsService.syncWorkflowRunsToDatabase(owner, repo, repositoryId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "syncedCount", syncedCount,
                    "message", String.format("Synced %d builds to database", syncedCount)
            ));
        } catch (GitHubApiException e) {
            return ResponseEntity.status(e.getStatusCode() > 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
